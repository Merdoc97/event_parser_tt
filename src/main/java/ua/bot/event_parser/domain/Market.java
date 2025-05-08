package ua.bot.event_parser.domain;

import lombok.Data;

import java.util.List;

@Data
public class Market {
    private String markerName;
    private List<String> marketList;
}
