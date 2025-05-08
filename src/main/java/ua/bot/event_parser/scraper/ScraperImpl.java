package ua.bot.event_parser.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import ua.bot.event_parser.processing.ResultCollector;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class ScraperImpl implements Scraper {

    @Override
    public <T> Set<T> scrape(String url, Map<String, String> parseConfig,
                             ResultCollector<T> resultCollector, String baseUrl) {
        parseConfig.remove("url");
        return getDocument(url)
                .stream()
                .flatMap(document -> collect(document, parseConfig, resultCollector, baseUrl))
                .collect(Collectors.toSet());
    }

    private Optional<Document> getDocument(String url) {
        try {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch();
                Page page = browser.newPage();
                page.navigate(url);
                page.waitForLoadState(LoadState.NETWORKIDLE);
                return Optional.ofNullable(Jsoup.parse(page.content()));
            }
        } catch (Exception e) {
            log.error("Cannot get document from url: {}", url, e);
            return Optional.empty();
        }
    }

    private <T> Stream<T> collect(Document doc, Map<String, String> scrapeConfig,
                                  ResultCollector<T> resultCollector, String baseUrl) {
        return Optional.ofNullable(scrapeConfig.get("baseElement"))
                .map(doc::select)
                .stream()
                .distinct()
                .filter(Objects::nonNull)
                .filter(e -> !e.isEmpty())
                .flatMap(e -> resultCollector.collectElements(e, scrapeConfig,baseUrl).stream());

    }
}
