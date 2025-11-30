package io.github.milesreimann.clansystem.bungee.mapper;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import io.github.milesreimann.clansystem.bungee.entity.ClanImpl;

import java.util.UUID;
import java.util.function.Function;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public class ClanMapper implements Function<QueryRow, Clan> {
    @Override
    public Clan apply(QueryRow row) {
        return new ClanImpl(
            row.getOrThrow("id_clan", Long.class),
            UUID.fromString(row.getOrThrow("owner", String.class)),
            row.getOrThrow("name", String.class),
            row.getOrThrow("tag", String.class)
        );
    }
}
