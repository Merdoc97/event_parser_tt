package ua.bot.event_parser.scraper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ua.bot.event_parser.config.LeonConfigurationProperties;
import ua.bot.event_parser.domain.PrematchEvent;
import ua.bot.event_parser.printer.Printer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
@Tag("local")
class LeonScrapeServiceImpl {
    @Autowired
    private LeonScrapeService leonScrapeService;
    @Autowired
    private LeonConfigurationProperties leonConfigurationProperties;
    @Autowired
    @Qualifier("leonConsolePrinter")
    private Printer printer;

    @Test
    void testMatchPartParser() throws MalformedURLException {
        var matchConfig = new HashMap<>(leonConfigurationProperties.getMatchesConfig());
        matchConfig.put("url", "https://leonbets.com/bets/soccer/spain/1970324836975689-laliga");
        matchConfig.put("leagueName", "France - League 1");
        matchConfig.put("sportType", "Football");
        var result = leonScrapeService.parseMatches(Set.of(matchConfig), leonConfigurationProperties.getBaseUrl());

        Assertions.assertThat(result)
                .isNotNull()
                .hasSizeGreaterThan(0)
                .hasSizeLessThan(3);
        var firstMatch = result.stream().findFirst().get();
        Assertions.assertThat(firstMatch.get("link")).isNotNull();
        URI.create(firstMatch.get("link").toString()).toURL();
        Assertions.assertThat(firstMatch.get("sportType")).isEqualToIgnoringCase("Football");
        Assertions.assertThat(firstMatch.get("leagueName")).isEqualToIgnoringCase("France - League 1");
        Assertions.assertThat(firstMatch.get("matchName")).isNotNull();
        Assertions.assertThat(firstMatch.get("date")).isNotNull();
        Assertions.assertThat(firstMatch.get("time")).isNotNull();
        var date = firstMatch.get("date").toString();
        var time = firstMatch.get("time").toString();
        LocalTime.parse(time);
        MonthDay.parse(date, DateTimeFormatter.ofPattern("dd.MM"));
        Assertions.assertThat(firstMatch.get("id")).isNotNull();
        Assertions.assertThat(firstMatch.get("link")).contains(firstMatch.get("id"));
        Assertions.assertThat(firstMatch.get("link")).isEqualToIgnoringCase(firstMatch.get("url"));
    }

    @Test
    void testParseMatchEventsMap() {
        Map<String, String> eventConfig = new HashMap<>(leonConfigurationProperties.getEventsConfig());
        eventConfig.put("link", "https://leonbets.com/bets/soccer/spain/laliga/1970324846566924-las-palmas-rayo-vallecano");
        eventConfig.put("id", "1970324846566924-las-palmas-rayo-vallecano");
        var mapConfig = Set.of(eventConfig);
        var result = leonScrapeService.parseMatchEventsMap(mapConfig, leonConfigurationProperties.getBaseUrl());
        Assertions.assertThat(result).isNotEmpty();
        var res = result.get("1970324846566924-las-palmas-rayo-vallecano");
        Assertions.assertThat(res).isNotEmpty();
        var firstRes = res.stream().findFirst().get();
        Assertions.assertThat(firstRes).isNotNull().containsKeys("baseElement", "marketName", "eventList");
        Assertions.assertThat(firstRes.get("eventList")).contains(";");
        Assertions.assertThat(firstRes.get("baseElement")).isNotEmpty();
        Assertions.assertThat(firstRes.get("marketName")).isNotEmpty();
        Assertions.assertThat(firstRes.get("id")).isNotEmpty().isEqualToIgnoringCase("1970324846566924-las-palmas-rayo-vallecano");
    }

    @Test
    @Disabled
    void scrapeEvents() {
        var res = leonScrapeService.scrape(Set.of("esports"));
        Assertions.assertThat(res).isNotEmpty();
        log.info("events is {}", res);
    }

    @Test
    @SneakyThrows
    void testPrinter() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        var file = new File(this.getClass().getResource("/event.json").getFile());
        var evemts=mapper.readValue(file, new TypeReference<List<PrematchEvent>>() {
        });
        printer.print(evemts);
    }
}
