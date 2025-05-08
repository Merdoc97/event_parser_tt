package ua.bot.event_parser.processing;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import ua.bot.event_parser.config.LeonConfigurationProperties;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MapResultCollectorImpl implements ResultCollector<Map<String, String>> {

    private final LeonConfigurationProperties configurationProperties;

    @Override
    @SneakyThrows
    public Set<Map<String, String>> collectElements(Elements elements, Map<String, String> parseConfig, String baseUrl) {
        return elements.stream()
                .filter(Objects::nonNull)
                .map(element -> collectToMap(element, parseConfig, baseUrl))
                .filter(stringStringMap -> {
                    if (parseConfig.get("link") != null) {
                        return !baseUrl.equalsIgnoreCase(stringStringMap.get("link"));
                    }
                    return false;
                })
                .collect(Collectors.toSet());
    }

    private Map<String, String> collectToMap(Element element, Map<String, String> parseConfig, String baseUrl) {
        return parseConfig.entrySet()
                .stream()
                .distinct()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> collect(element, entry, baseUrl)));
    }

    private String collect(Element element, Map.Entry<String, String> entry, String baseUrl) {
        if (entry.getKey().equalsIgnoreCase("link")) {
            var link = element.select(entry.getValue()).attr("href");
            return baseUrl + link;
        } else {
            if (configurationProperties.getReservedWords().contains(entry.getKey())) {
                return entry.getValue();
            }
            return element.select(entry.getValue()).text();
        }
    }
}
