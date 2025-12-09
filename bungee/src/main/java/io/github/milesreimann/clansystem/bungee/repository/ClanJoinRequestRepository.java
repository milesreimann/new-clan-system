package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.ClanJoinRequest;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanJoinRequestMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
@RequiredArgsConstructor
public class ClanJoinRequestRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS clan_join_requests(
        player VARCHAR(36),
        clan_id BIGINT,
        `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (player, clan_id),
        FOREIGN KEY (clan_id) REFERENCES clans(id_clan) ON UPDATE CASCADE ON DELETE CASCADE
        );
        """;

    private static final String CREATE_EVENT = """
        CREATE EVENT IF NOT EXISTS delete_expired_clan_join_requests\s
        ON SCHEDULE EVERY 1 HOUR\s
        DO\s
        DELETE FROM clan_join_requests\s
        WHERE `timestamp` < NOW() - INTERVAL 7 DAY;
        """;

    private static final String INSERT_JOIN_REQUEST = """
        INSERT INTO clan_join_requests(player, clan_id)\s
        VALUES (?, ?);
        """;

    private static final String DELETE_JOIN_REQUEST_BY_PLAYER_AND_CLAN_ID = """
        DELETE FROM clan_join_requests\s
        WHERE player = ? AND clan_id = ?;
        """;

    private static final String DELETE_JOIN_REQUEST_BY_CLAN_ID = """
        DELETE FROM clan_join_requests\s
        WHERE clan_id = ?;
        """;

    private static final String SELECT_JOIN_REQUEST_BY_PLAYER_AND_CLAN_ID = """
        SELECT player, clan_id, `timestamp`\s
        FROM clan_join_requests\s
        WHERE player = ? AND clan_id = ?;
        """;

    private static final String SELECT_JOIN_REQUESTS_BY_CLAN_ID = """
        SELECT clan_id, sender, receiver, timestamp\s
        FROM clan_join_requests\s
        WHERE receiver = ?;
        """;

    private final MySQLDatabase database;
    private final ClanJoinRequestMapper mapper;

    public void createTableAndEvent() {
        database.update(CREATE_TABLE).join();
        database.update(CREATE_EVENT).join();
    }

    public CompletionStage<Boolean> insert(ClanJoinRequest request) {
        return database.update(INSERT_JOIN_REQUEST, request.getPlayer().toString(), request.getClan())
            .thenApply(rows -> rows == 0);
    }

    public CompletionStage<Boolean> deleteByPlayerAndClanId(UUID player, long clanId) {
        return database.update(DELETE_JOIN_REQUEST_BY_PLAYER_AND_CLAN_ID, player, clanId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> deleteByClanId(long clanId) {
        return database.update(DELETE_JOIN_REQUEST_BY_CLAN_ID, clanId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<ClanJoinRequest> findByPlayerAndClanId(UUID player, long clanId) {
        return database.query(SELECT_JOIN_REQUEST_BY_PLAYER_AND_CLAN_ID, player, clanId)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<List<ClanJoinRequest>> findByClanId(long clanId) {
        return database.query(SELECT_JOIN_REQUESTS_BY_CLAN_ID, clanId)
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }
}
