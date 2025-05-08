package ua.bot.event_parser;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ua.bot.event_parser.config.LeonConfigurationProperties;
import ua.bot.event_parser.scraper.LeonScrapeService;

@SpringBootApplication
@EnableConfigurationProperties(value = LeonConfigurationProperties.class)
@RequiredArgsConstructor
public class EventParserApplication {

    private final LeonScrapeService leonScrapeService;

    public static void main(String[] args) {
        SpringApplication.run(EventParserApplication.class, args);
    }

    /*@PostConstruct
    void postConstruct() {
        leonScrapeService.scrape();
    }*/
}
