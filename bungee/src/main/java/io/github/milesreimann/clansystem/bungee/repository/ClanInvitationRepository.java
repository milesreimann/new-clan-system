package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.ClanInvitation;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanInvitationMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
@RequiredArgsConstructor
public class ClanInvitationRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS clan_invitations(
        clan_id BIGINT,
        sender BIGINT,
        receiver VARCHAR(36),
        `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (clan_id, receiver),
        FOREIGN KEY (clan_id) REFERENCES clans(id_clan) ON UPDATE CASCADE ON DELETE CASCADE,
        FOREIGN KEY (sender) REFERENCES clan_members(player) ON UPDATE CASCADE ON DELETE SET NULL
        );
        """;

    private static final String CREATE_EVENT = """
        CREATE EVENT IF NOT EXISTS delete_expired_clan_invitations\s
        ON SCHEDULE EVERY 1 HOUR\s
        DO\s
        DELETE FROM clan_invitations\s
        WHERE `timestamp` < NOW() - INTERVAL 7 DAY;
        """;

    private static final String INSERT_INVITATION = """
        INSERT INTO clan_invitations(clan_id, sender, receiver)\s
        VALUES (?, ?, ?);
        """;

    private static final String DELETE_INVITATION_BY_CLAN_ID_AND_RECEIVER = """
        DELETE FROM clan_invitations\s
        WHERE clan_id = ? AND receiver = ?;
        """;

    private static final String DELETE_INVITATION_BY_RECEIVER = """
        DELETE FROM clan_invitations\s
        WHERE receiver = ?;
        """;

    private static final String SELECT_INVITATION_BY_CLAN_ID_AND_RECEIVER = """
        SELECT clan_id, sender, receiver, `timestamp`\s
        FROM clan_invitations\s
        WHERE clan_id = ? AND receiver = ?;
        """;

    private static final String SELECT_INVITATIONS_BY_RECEIVER = """
        SELECT clan_id, sender, receiver, `timestamp`\s
        FROM clan_invitations\s
        WHERE receiver = ?;
        """;

    private final MySQLDatabase database;
    private final ClanInvitationMapper mapper;

    public void createTableAndEvent() {
        database.update(CREATE_TABLE).join();
        database.update(CREATE_EVENT).join();
    }

    public CompletionStage<Boolean> insert(ClanInvitation invitation) {
        return database.update(INSERT_INVITATION, invitation.getClan(), invitation.getSender(), invitation.getRecipient().toString())
            .thenApply(rows -> rows == 0);
    }

    public CompletionStage<Boolean> deleteByClanIdAndReceiver(long clanId, UUID receiver) {
        return database.update(DELETE_INVITATION_BY_CLAN_ID_AND_RECEIVER, clanId, receiver.toString())
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> deleteByReceiver(UUID receiver) {
        return database.update(DELETE_INVITATION_BY_RECEIVER, receiver)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<ClanInvitation> findByClanIdAndReceiver(long clanId, UUID receiver) {
        return database.query(SELECT_INVITATION_BY_CLAN_ID_AND_RECEIVER, clanId, receiver.toString())
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<List<ClanInvitation>> findByReceiver(UUID receiver) {
        return database.query(SELECT_INVITATIONS_BY_RECEIVER, receiver.toString())
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }
}
