package ua.bot.event_parser.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrematchEvent {
    private String sportType;
    private String leagueName;
    @Builder.Default
    private Set<Match> matches = new LinkedHashSet<>();

    @Override
    public String toString() {

        return sportType + " " + leagueName + " \r\n "
                + Optional.ofNullable(matches).orElse(Set.of()).stream()
                .map(Match::toString)
                .collect(Collectors.joining("\r\n"));
    }
}
