package ua.bot.event_parser.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "leon-bets")
@Data
@Validated
public class LeonConfigurationProperties {
    @NotEmpty(message = "url for scraping can't be empty")
    @URL
    private String url;
    private String baseUrl;

    @NotEmpty(message = "scrape config for sport types can't be empty")
    private Map<String, String> sportTypesConfig;

    @NotEmpty(message = "scrape config for leagues can't be empty")
    private Map<String, String> leaguesConfig;
    private Set<String> reservedWords = Set.of();

    @NotEmpty(message = "scrape config for matches can't be empty")
    private Map<String, String> matchesConfig;

    @NotEmpty(message = "scrape config for events can't be empty")
    private Map<String, String> eventsConfig;
}
