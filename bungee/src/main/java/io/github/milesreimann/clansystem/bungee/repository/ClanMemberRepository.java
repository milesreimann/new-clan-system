package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanMemberMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
@RequiredArgsConstructor
public class ClanMemberRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS clan_members(
        player VARCHAR(36) PRIMARY KEY,
        clan_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (clan_id) REFERENCES clans(id_clan) ON DELETE CASCADE,
        FOREIGN KEY (role_id) REFERENCES clan_roles(id_role) ON DELETE RESTRICT ON UPDATE CASCADE,
        INDEX (clan_id)
        );
        """;

    private static final String INSERT_MEMBER = """
        INSERT INTO clan_members(player, clan_id, role_id)\s
        VALUES (?, ?, ?);
        """;

    private static final String DELETE_MEMBER_BY_UUID = """
        DELETE\s
        FROM clan_members\s
        WHERE player = ?;
        """;

    private static final String UPDATE_ROLE = """
        UPDATE clan_members\s
        SET role_id = ?\s
        WHERE player = ?;
        """;

    private static final String SELECT_MEMBER_BY_UUID = """
        SELECT *\s
        FROM clan_members\s
        WHERE player = ?;
        """;

    private static final String SELECT_MEMBERS_BY_CLAN_ID = """
        SELECT *\s
        FROM clan_members\s
        WHERE clan_id = ?;
        """;

    private static final String EXISTS_MEMBER_BY_UUID = """
        SELECT EXISTS (
        SELECT 1\s
        FROM clan_members\s
        WHERE player = ?\s
        ) AS `exists`;
        """;

    private final MySQLDatabase database;
    private final ClanMemberMapper mapper;

    public void createTable() {
        database.update(CREATE_TABLE).join();
    }

    public CompletionStage<ClanMember> insert(ClanMember member) {
        return database.update(INSERT_MEMBER, member.getUuid().toString(), member.getClan(), member.getRole())
            .thenCompose(_ -> findByUuid(member.getUuid()));
    }

    public CompletionStage<Boolean> deleteByUuid(UUID uuid) {
        return database.update(DELETE_MEMBER_BY_UUID, uuid.toString())
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> updateRole(UUID uuid, long roleId) {
        return database.update(UPDATE_ROLE, roleId, uuid.toString())
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<ClanMember> findByUuid(UUID uuid) {
        return database.query(SELECT_MEMBER_BY_UUID, uuid.toString())
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<Optional<ClanMember>> findByUuidOptional(UUID uuid) {
        return database.query(SELECT_MEMBER_BY_UUID, uuid.toString())
            .thenApply(result -> result.firstOptional()
                .map(mapper));
    }

    public CompletionStage<List<ClanMember>> findByClanId(long clanId) {
        return database.query(SELECT_MEMBERS_BY_CLAN_ID, clanId)
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }

    public CompletionStage<Boolean> existsByUuid(UUID uuid) {
        return database.query(EXISTS_MEMBER_BY_UUID, uuid.toString())
            .thenApply(result -> result.firstOptional()
                .map(row -> row.getOrThrow("exists", Long.class) != 0)
                .orElse(false));
    }
}
