package io.github.milesreimann.clansystem.bungee.mapper;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import io.github.milesreimann.clansystem.bungee.entity.ClanMemberImpl;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public class ClanMemberMapper implements Function<QueryRow, ClanMember> {
    @Override
    public ClanMember apply(QueryRow row) {
        return new ClanMemberImpl(
            UUID.fromString(row.getOrThrow("player", String.class)),
            row.getOrThrow("clan_id", Long.class),
            row.getOrThrow("role_id", Long.class),
            row.getOrThrow("joined_at", Timestamp.class).toInstant()
        );
    }
}
