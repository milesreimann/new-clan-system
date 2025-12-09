package io.github.milesreimann.clansystem.bungee.mapper;

import io.github.milesreimann.clansystem.api.entity.ClanInvitation;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import io.github.milesreimann.clansystem.bungee.entity.ClanInvitationImpl;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanInvitationMapper implements Function<QueryRow, ClanInvitation> {
    @Override
    public ClanInvitation apply(QueryRow row) {
        return new ClanInvitationImpl(
            row.getOrThrow("clan_id", Long.class),
            row.getOrThrow("sender", Long.class),
            UUID.fromString(row.getOrThrow("receiver", String.class)),
            row.getOrThrow("timestamp", Timestamp.class).toInstant()
        );
    }
}
