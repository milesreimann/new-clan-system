package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanMapper;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
@RequiredArgsConstructor
public class ClanRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS clans(
        id_clan BIGINT PRIMARY KEY AUTO_INCREMENT,
        owner VARCHAR(36) NOT NULL,
        name VARCHAR(255) NOT NULL COLLATE utf8mb4_general_ci,
        tag VARCHAR(255) NOT NULL COLLATE utf8mb4_general_ci,
        owner_role_id BIGINT,
        default_role_id BIGINT,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        );""";

    private static final String ALTER_TABLE_ADD_FOREIGN_KEYS = """
        ALTER TABLE clans\s
        ADD FOREIGN KEY (owner_role_id) REFERENCES clan_roles(id_role) ON UPDATE CASCADE ON DELETE RESTRICT,\s
        ADD FOREIGN KEY (default_role_id) REFERENCES clan_roles(id_role) ON UPDATE CASCADE;
        """;

    private static final String INSERT_CLAN = """
        INSERT INTO clans(owner, name, tag)\s
        VALUES (?, ?, ?);
        """;

    private static final String UPDATE_NAME = """
        UPDATE clans\s
        SET name = ?\s
        WHERE id_clan = ?;
        """;

    private static final String UPDATE_TAG = """
        UPDATE clans\s
        SET tag = ?\s
        WHERE id_clan = ?;
        """;

    private static final String DELETE_CLAN_BY_ID = """
        DELETE\s
        FROM clans\s
        WHERE id_clan = ?;
        """;

    private static final String SELECT_CLAN_BY_ID = """
        SELECT id_clan, owner, name, tag, owner_role_id, default_role_id\s
        FROM clans\s
        WHERE id_clan = ?;
        """;

    private static final String SELECT_CLAN_BY_NAME = """
        SELECT id_clan, owner, name, tag, owner_role_id, default_role_id\s
        FROM clans\s
        WHERE name = ?;
        """;

    private static final String SELECT_CLAN_BY_TAG = """
        SELECT id_clan, owner, name, tag, owner_role_id, default_role_id\s
        FROM clans\s
        WHERE tag = ?;
        """;

    private static final String EXISTS_CLAN_BY_NAME = """
        SELECT EXISTS (
        SELECT 1\s
        FROM clans\s
        WHERE name = ?\s
        ) AS `exists`;
        """;

    private static final String EXISTS_CLAN_BY_TAG = """
        SELECT EXISTS (
        SELECT 1\s
        FROM clans\s
        WHERE tag = ?\s
        ) AS `exists`;
        """;

    private static final String UPDATE_OWNER_ROLE_ID = """
        UPDATE clans\s
        SET owner_role_id = ?\s
        WHERE id_clan = ?;
        """;

    private static final String UPDATE_DEFAULT_ROLE_ID = """
        UPDATE clans\s
        SET default_role_id = ?\s
        WHERE id_clan = ?;
        """;

    private final MySQLDatabase database;
    private final ClanMapper mapper;

    public void createTable() {
        database.update(CREATE_TABLE).join();
    }

    public void addRoleForeignKeys() {
        database.update(ALTER_TABLE_ADD_FOREIGN_KEYS).join();
    }

    public CompletionStage<Long> insert(Clan clan) {
        return database.insert(INSERT_CLAN, clan.getOwner().toString(), clan.getName(), clan.getTag());
    }

    public CompletionStage<Boolean> deleteById(long id) {
        return database.update(DELETE_CLAN_BY_ID, id)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> updateName(long id, String newName) {
        return database.update(UPDATE_NAME, newName, id)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> updateTag(long id, String newTag) {
        return database.update(UPDATE_TAG, newTag, id)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Clan> findById(long id) {
        return database.query(SELECT_CLAN_BY_ID, id)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<Clan> findByName(String name) {
        return database.query(SELECT_CLAN_BY_NAME, name)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<Clan> findByTag(String tag) {
        return database.query(SELECT_CLAN_BY_TAG, tag)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<Boolean> existsByName(String name) {
        return database.query(EXISTS_CLAN_BY_NAME, name)
            .thenApply(result -> result.firstOptional()
                .map(row -> row.getOrThrow("exists", Long.class) != 0)
                .orElse(false));
    }

    public CompletionStage<Boolean> existsByTag(String tag) {
        return database.query(EXISTS_CLAN_BY_TAG, tag)
            .thenApply(result -> result.firstOptional()
                .map(row -> row.getOrThrow("exists", Long.class) != 0)
                .orElse(false));
    }

    public CompletionStage<Boolean> updateOwnerRoleId(long clanId, long roleId) {
        return database.update(UPDATE_OWNER_ROLE_ID, roleId, clanId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> updateDefaultRoleId(long clanId, long roleId) {
        return database.update(UPDATE_DEFAULT_ROLE_ID, roleId, clanId)
            .thenApply(rows -> rows == 1);
    }
}