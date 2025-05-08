package ua.bot.event_parser.processing;

import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Map;

public interface ResultCollector<T> {
    Collection<T> collectElements(Elements elements, Map<String, String> scrapeConfig,String baseUrl);
}
