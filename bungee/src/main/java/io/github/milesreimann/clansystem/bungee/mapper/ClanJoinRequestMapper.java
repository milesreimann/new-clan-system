package io.github.milesreimann.clansystem.bungee.mapper;

import io.github.milesreimann.clansystem.api.entity.ClanJoinRequest;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import io.github.milesreimann.clansystem.bungee.entity.ClanJoinRequestImpl;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanJoinRequestMapper implements Function<QueryRow, ClanJoinRequest> {
    @Override
    public ClanJoinRequest apply(QueryRow row) {
        return new ClanJoinRequestImpl(
            UUID.fromString(row.getOrThrow("player", String.class)),
            row.getOrThrow("clan_id", Long.class),
            row.getOrThrow("timestamp", Timestamp.class).toInstant()
        );
    }
}
