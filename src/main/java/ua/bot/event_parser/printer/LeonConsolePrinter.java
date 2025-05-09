package ua.bot.event_parser.printer;

import org.springframework.stereotype.Component;
import ua.bot.event_parser.domain.PrematchEvent;

import java.util.List;

@Component
public  class LeonConsolePrinter implements Printer {

    @Override
    public void print(List<PrematchEvent> events) {
        System.out.println("----------------------------------------------------------");
        events.forEach(System.out::println);
        System.out.println("----------------------------------------------------------");
    }
}
