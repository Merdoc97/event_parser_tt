package ua.bot.event_parser.scraper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.bot.event_parser.config.LeonConfigurationProperties;
import ua.bot.event_parser.domain.PrematchEvent;
import ua.bot.event_parser.processing.ResultCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeonScrapeService {

    private final Scraper scraper;
    private final LeonConfigurationProperties leonConfigurationProperties;
    @Qualifier("mapResultCollectorImpl")
    private final ResultCollector<Map<String, String>> mapResultCollector;
    private final ExecutorService executorService = new ThreadPoolExecutor(3, 3, 10, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    public List<PrematchEvent> scrape() {
        var sportTypes = scraper.scrape(leonConfigurationProperties.getUrl(), leonConfigurationProperties.getSportTypesConfig(),
                mapResultCollector, leonConfigurationProperties.getBaseUrl());
        log.info("Scraped size sport types {}", sportTypes.size());
        log.info("Scraped values for sport types {}", sportTypes);
        //add all sport types into links into leagues
        var leguaConfigs = sportTypes.stream()
                .map(sType -> {
                    var map = new HashMap<>(leonConfigurationProperties.getLeaguesConfig());
                    map.put("sportType", sType.get("sportName"));
                    map.put("url", sType.get("link"));
                    return map;
                })
                .collect(Collectors.toSet());
        var leagues = parseMap(Set.of(leguaConfigs.stream()
                .filter(map -> map.get("sportType").equalsIgnoreCase("football"))
                .findFirst().get()), leonConfigurationProperties.getBaseUrl());
        log.info("Scraped leagues {}", leagues);
        var matchesConfig = leagues.values().stream()
                .flatMap(maps -> maps.stream())
                .map(league -> {
                    var map = new HashMap<>(leonConfigurationProperties.getMatchesConfig());
                    map.putAll(league);
                    map.put("url", league.get("link"));
                    map.put("leagueName", league.get("league"));
                    return map;
                })
                .collect(Collectors.toSet());
        var matches = parseMatches(matchesConfig, leonConfigurationProperties.getBaseUrl());

        return null;
    }

    @SneakyThrows
    Map<String, Set<Map<String, String>>> parseMap(Set<HashMap<String, String>> configs, String baseUrl) {
        return configs.parallelStream()
                .flatMap(config -> scraper.scrape(config.get("url"), config, mapResultCollector, baseUrl).stream())
                .collect(Collectors.groupingBy(maps -> maps.get("sportType"), Collectors.toSet()));
    }

    @SneakyThrows
    Map<String, Set<Map<String, String>>> parseMatches(Set<HashMap<String, String>> configs, String baseUrl) {
        return configs.parallelStream()
                .flatMap(config -> scraper.scrape(config.get("url"), config, mapResultCollector, baseUrl).stream())
                .filter(map -> !map.get("date").isEmpty())
                .filter(map -> !map.get("time").isEmpty())
                .limit(2)
                .collect(Collectors.groupingBy(maps -> maps.get("sportType"), Collectors.toSet()));
    }


}
