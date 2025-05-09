package ua.bot.event_parser.printer;

import ua.bot.event_parser.domain.PrematchEvent;

import java.util.List;

public interface Printer {
    void print(List<PrematchEvent> events);
}
