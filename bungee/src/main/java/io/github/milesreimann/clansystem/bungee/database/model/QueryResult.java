package io.github.milesreimann.clansystem.bungee.database.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Miles R.
 * @since 27.11.2025
 */
public record QueryResult(List<QueryRow> rows) {
    public Stream<QueryRow> stream() {
        return rows.stream();
    }

    public Optional<QueryRow> firstOptional() {
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.getFirst());
    }
}
