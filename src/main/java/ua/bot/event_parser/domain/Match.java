package ua.bot.event_parser.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    private String id;
    private String matchName;
    private String sportType;
    private String leagueName;
    private ZonedDateTime eventTime;
    private Set<Event> events = new LinkedHashSet<>();

    @Override
    public String toString() {
        return "    " + matchName + " " + eventTime + " " + id + " \r\n"
                + Optional.ofNullable(events).orElse(Set.of()).stream()
                .map(Event::toString)
                .collect(Collectors.joining("\r\n"));
    }
}
