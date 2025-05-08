package ua.bot.event_parser.scraper;

import ua.bot.event_parser.processing.ResultCollector;

import java.util.Collection;
import java.util.Map;

public interface Scraper {
    <T> Collection<T> scrape(String url, Map<String, String> parseConfig,
                             ResultCollector<T> resultCollector,String baseUrl);

}
