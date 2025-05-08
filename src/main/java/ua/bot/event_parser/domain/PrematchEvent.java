package ua.bot.event_parser.domain;

import lombok.Data;

import java.util.List;

@Data
public class PrematchEvent {
    private String sportName;
    private String league;
    private List<Match> matches;

}
