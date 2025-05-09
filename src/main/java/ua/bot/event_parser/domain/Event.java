package ua.bot.event_parser.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String matchId;
    private String markerName;
    private Set<String> marketValue;

    @Override
    public String toString() {
        return "        " + markerName + " \r\n"
                + Optional.ofNullable(marketValue).orElse(Set.of()).stream()
                .map(s -> "            " + s)
                .collect(Collectors.joining("\r\n"));
    }
}
