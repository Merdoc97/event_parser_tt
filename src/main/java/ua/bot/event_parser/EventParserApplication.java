package ua.bot.event_parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ua.bot.event_parser.config.LeonConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = LeonConfigurationProperties.class)
public class EventParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventParserApplication.class, args);
    }
}
