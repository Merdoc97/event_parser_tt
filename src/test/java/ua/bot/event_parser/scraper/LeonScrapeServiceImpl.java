package ua.bot.event_parser.scraper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.bot.event_parser.config.LeonConfigurationProperties;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;

@SpringBootTest
public class LeonScrapeServiceImpl {
    @Autowired
    private LeonScrapeService leonScrapeService;
    @Autowired
    private LeonConfigurationProperties leonConfigurationProperties;

    @Test
    void testMatchPartParser() throws MalformedURLException {
        var matchConfig = new HashMap<String, String>();
        matchConfig.put("baseElement", "[class*=sportline-event-block-container]");
        matchConfig.put("link", "[class*=sportline-event-block-container]>a");
        matchConfig.put("matchName", "[class*=sportline-event-competitors] span");
        matchConfig.put("date", "[class*=kickoff-countdown__date]");
        matchConfig.put("time", "[class*=kickoff-countdown__time]");
        matchConfig.put("sportType", "Football");
        matchConfig.put("url", "https://leonbets.com/bets/soccer/spain/1970324836975689-laliga");
        matchConfig.put("leagueName", "France - League 1");
        var result = leonScrapeService.parseMatches(Set.of(matchConfig), leonConfigurationProperties.getBaseUrl());
        var results = result.get("Football");
        Assertions.assertThat(results)
                .isNotNull()
                .hasSizeGreaterThan(0)
                .hasSizeLessThan(3);
        var firstMatch = results.stream().findFirst().get();
        Assertions.assertThat(firstMatch.get("link")).isNotNull();
        URI.create(firstMatch.get("link").toString()).toURL();
        Assertions.assertThat(firstMatch.get("sportType")).isEqualToIgnoringCase("Football");
        Assertions.assertThat(firstMatch.get("leagueName")).isEqualToIgnoringCase("France - League 1");
        Assertions.assertThat(firstMatch.get("matchName")).isNotNull();
        Assertions.assertThat(firstMatch.get("matchName")).isNotNull();
        Assertions.assertThat(firstMatch.get("date")).isNotNull();
        Assertions.assertThat(firstMatch.get("time")).isNotNull();
        var date = firstMatch.get("date").toString();
        var time = firstMatch.get("time").toString();
        LocalTime.parse(time);
        MonthDay.parse(date, DateTimeFormatter.ofPattern("dd.MM"));

    }
}
