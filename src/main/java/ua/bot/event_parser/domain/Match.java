package ua.bot.event_parser.domain;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class Match {
    private String eventName;
    private ZonedDateTime eventTime;
    private List<Market>markets;
}
