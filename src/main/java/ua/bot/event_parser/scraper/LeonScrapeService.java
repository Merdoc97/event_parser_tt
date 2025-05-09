package ua.bot.event_parser.scraper;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.bot.event_parser.config.LeonConfigurationProperties;
import ua.bot.event_parser.domain.Event;
import ua.bot.event_parser.domain.Match;
import ua.bot.event_parser.domain.PrematchEvent;
import ua.bot.event_parser.processing.ResultCollector;

import java.time.LocalTime;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
    private final ExecutorService executorService = new ThreadPoolExecutor(3, 3, 50, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    public List<PrematchEvent> scrape() {
        var sportTypes = parseSportTypes();

        var leguaConfigs = prepareLeagueConfigs(sportTypes);

        var leagues = parseLeagues(leguaConfigs, leonConfigurationProperties.getBaseUrl());

        var matchesConfig = prepareMatchesConfig(leagues);

        var matches = parseMatches(matchesConfig, leonConfigurationProperties.getBaseUrl());

        return parseMatchEvents(matches, leonConfigurationProperties.getBaseUrl());
    }

    public List<PrematchEvent> scrape(Set<String> sType) {
        var sportTypes = parseSportTypes();

        var leguaConfigs = prepareLeagueConfigs(sportTypes)
                .stream()
                .filter(map -> sType.contains(map.get("sportType").toLowerCase()))
                .collect(Collectors.toSet());

        var leagues = parseLeagues(leguaConfigs, leonConfigurationProperties.getBaseUrl());

        var matchesConfig = prepareMatchesConfig(leagues);

        var matches = parseMatches(matchesConfig, leonConfigurationProperties.getBaseUrl());

        return parseMatchEvents(matches, leonConfigurationProperties.getBaseUrl());
    }

    private Set<Map<String, String>> prepareMatchesConfig(Map<String, Set<Map<String, String>>> leagues) {
        return leagues.values().stream()
                .flatMap(Collection::stream)
                .map(league -> {
                    Map<String, String> map = new HashMap<>(leonConfigurationProperties.getMatchesConfig());
                    map.put("sportType", league.get("sportType"));
                    map.put("url", league.get("link"));
                    map.put("leagueName", league.get("league"));
                    return map;
                })
                .collect(Collectors.toSet());
    }

    Collection<HashMap<String, String>> prepareLeagueConfigs(Collection<Map<String, String>> sportTypes) {
        return sportTypes.stream()
                .map(sType -> {
                    var map = new HashMap<>(leonConfigurationProperties.getLeaguesConfig());
                    map.put("sportType", sType.get("sportName"));
                    map.put("url", sType.get("link"));
                    return map;
                })
                .collect(Collectors.toSet());
    }

    Collection<Map<String, String>> parseSportTypes() {
        log.debug("start parsing sport types");
        var sportTypes = scraper.scrape(leonConfigurationProperties.getUrl(), leonConfigurationProperties.getSportTypesConfig(),
                mapResultCollector, leonConfigurationProperties.getBaseUrl());
        log.debug("Scraped size sport types {}", sportTypes.size());
        log.debug("Scraped values for sport types {}", sportTypes);
        return sportTypes;
    }

    @SneakyThrows
    Map<String, Set<Map<String, String>>> parseLeagues(Collection<HashMap<String, String>> configs, String baseUrl) {
        log.debug("start parseLeagues configs {}", configs);
        var res = configs.parallelStream()
                .flatMap(config -> executeAsync(config.get("url"), config, mapResultCollector, baseUrl).stream())
                .collect(Collectors.groupingBy(maps -> maps.get("sportType"), Collectors.toSet()));
        log.debug("finish parseLeagues by sport type: {}", res.size());
        return res;
    }

    @SneakyThrows
    Set<Map<String, String>> parseMatches(Set<Map<String, String>> configs, String baseUrl) {
        log.debug("start parse matches config:{}", configs);

        var res = configs.stream()
                .flatMap(config -> executeAsync(config.get("url"), config, mapResultCollector, baseUrl)
                        .stream()
                        .filter(map -> !map.get("date").isEmpty())
                        .filter(map -> !map.get("time").isEmpty())
                        .filter(map -> !map.get("link").isEmpty())
                        .filter(map -> !map.get("matchName").isEmpty())
                        .sorted(Comparator.comparing(o -> o.get("date")))
                        .limit(2)
                        .peek(map -> {
                            var link = map.get("link");
                            var tmp = link.split("/");
                            map.put("id", tmp[tmp.length - 1]);
                            map.put("url", link);
                        })
                )
                .collect(Collectors.toSet());
        log.debug("finish parse matches size: {}", res.size());
        return res;
    }

    List<PrematchEvent> parseMatchEvents(Set<Map<String, String>> matchesConfig, String baseUrl) {
        var matchEvents = parseMatchEventsMap(matchesConfig, baseUrl)
                .values().stream()
                .flatMap(Collection::stream)
                .map(map -> Event.builder()
                        .matchId(map.get("id"))
                        .markerName(map.get("marketName"))
                        .marketValue(Arrays.stream(map.get("eventList").split(";"))
                                .collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.groupingBy(Event::getMatchId, Collectors.toSet()));

        return matchesConfig.stream()
                .map(matchMap -> {
                    var date = matchMap.get("date");
                    var time = matchMap.get("time");
                    var localTime = LocalTime.parse(time);
                    var monthDay = MonthDay.parse(date, DateTimeFormatter.ofPattern("dd.MM"));
                    return Match.builder()
                            .id(matchMap.get("id"))
                            .events(matchEvents.get(matchMap.get("id")))
                            .matchName(matchMap.get("matchName"))
                            .sportType(matchMap.get("sportType"))
                            .leagueName(matchMap.get("leagueName"))
                            .eventTime(ZonedDateTime.of(ZonedDateTime.now().getYear(), monthDay.getMonthValue(),
                                    monthDay.getDayOfMonth(), localTime.getHour(),
                                    localTime.getMinute(), localTime.getSecond(), 0, ZoneId.of("UTC")))
                            .build();
                }).collect(Collectors.groupingBy(Match::getSportType, Collectors.groupingBy(Match::getLeagueName, Collectors.toSet())))
                .entrySet().stream()
                .flatMap(map -> map.getValue().entrySet().stream()
                        .map(match -> PrematchEvent.builder()
                                .matches(match.getValue())
                                .sportType(map.getKey())
                                .leagueName(match.getKey())
                                .build()))
                .toList();
    }

    @SneakyThrows
    Map<String, Set<Map<String, String>>> parseMatchEventsMap(Set<Map<String, String>> matchesConfig, String baseUrl) {
        log.debug("start parse match events config: {}", matchesConfig);
        var res = matchesConfig.stream()
                .map(match -> {
                    var map = new HashMap<>(leonConfigurationProperties.getEventsConfig());
                    map.put("url", match.get("link"));
                    map.put("id", match.get("id"));
                    return map;
                })
                .flatMap(config -> executeAsync(config.get("url"), config, mapResultCollector, baseUrl).stream())
                .filter(map -> map.get("eventList").contains(";"))
                .collect(Collectors.groupingBy(maps -> maps.get("id"), Collectors.toSet()));

        log.debug("finish parse match events size: {}", res.size());
        return res;

    }

    @SneakyThrows
    private Collection<Map<String, String>> executeAsync(String url, Map<String, String> config,
                                                         ResultCollector<Map<String, String>> resultCollector, String baseUrl) {
        return executorService.submit(() -> scraper.scrape(url, config, resultCollector, baseUrl)
                        .parallelStream()
                        .collect(Collectors.toSet()))
                .get(60, TimeUnit.SECONDS);
    }

    @PreDestroy
    void preDestroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }


    }

}
