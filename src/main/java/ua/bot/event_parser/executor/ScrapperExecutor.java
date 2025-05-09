package ua.bot.event_parser.executor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ua.bot.event_parser.printer.Printer;
import ua.bot.event_parser.scraper.LeonScrapeService;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ScrapperExecutor {
    @Qualifier("leonConsolePrinter")
    private final Printer printer;
    private final LeonScrapeService scrapeService;

    @PostConstruct
    void execute() {
        var events = scrapeService.scrape();
        //uncommit line if you want to scrape by sport list
//        var events = scrapeService.scrape(Set.of("football","basketball"));
        printer.print(events);
    }
}
