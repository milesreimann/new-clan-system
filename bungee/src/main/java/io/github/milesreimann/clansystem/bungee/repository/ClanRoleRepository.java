package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanRoleMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
@RequiredArgsConstructor
public class ClanRoleRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS clan_roles(
        id_role BIGINT AUTO_INCREMENT PRIMARY KEY,
        clan_id BIGINT,
        name VARCHAR(32) NOT NULL,
        parent_role_id BIGINT DEFAULT NULL,
        sort_order INT DEFAULT 0,
        owner BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (clan_id) REFERENCES clans(id_clan) ON DELETE CASCADE,
        FOREIGN KEY (parent_role_id) REFERENCES clan_roles(id_role) ON DELETE SET NULL,
        UNIQUE (clan_id, name),
        INDEX (clan_id)
        );
        """;

    private static final String INSERT_ROLE = """
        INSERT INTO clan_roles(clan_id, name, parent_role_id, sort_order, owner)\s
        VALUES (?, ?, ?, ?, ?);
        """;

    private static final String DELETE_ROLE = """
        DELETE\s
        FROM clan_roles\s
        WHERE id_role = ?;
        """;

    private static final String UPDATE_NAME = """
        UPDATE clan_roles\s
        SET name = ?\s
        WHERE id_role = ?;
        """;

    private static final String UPDATE_PARENT_ROLE = """
        UPDATE clan_roles\s
        SET parent_role_id = ?\s
        WHERE id_role = ?;
        """;

    private static final String UPDATE_SORT_ORDER = """
        UPDATE clan_roles\s
        SET sort_order = ?\s
        WHERE id_role = ?;
        """;

    private static final String SELECT_ROLE_BY_ID = """
        SELECT id_role, clan_id, name, parent_role_id, sort_order, owner\s
        FROM clan_roles\s
        WHERE id_role = ?;
        """;

    private static final String SELECT_ROLES_BY_CLAN_ID = """
        SELECT id_role, clan_id, name, parent_role_id, sort_order, owner\s
        FROM clan_roles\s
        WHERE clan_id = NULL OR clan_id = ?;
        """;

    private static final String SELECT_OWNER_ROLE_BY_CLAN_ID = """
        SELECT id_role, clan_id, name, parent_role_id, sort_order, owner\s
        FROM clan_roles\s
        WHERE clan_id = ? AND owner = TRUE;
        """;

    private final MySQLDatabase database;
    private final ClanRoleMapper mapper;

    public void createTable() {
        database.update(CREATE_TABLE).join();
    }

    public CompletionStage<ClanRole> insert(ClanRole role) {
        return database.insert(INSERT_ROLE, role.getClan(), role.getName(), role.getParentRole().orElse(null), role.getSortOrder().orElse(null), role.isOwnerRole())
            .thenCompose(this::findById);
    }

    public CompletionStage<Boolean> deleteById(long id) {
        return database.update(DELETE_ROLE, id)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> updateName(long roleId, String newName) {
        return database.update(UPDATE_NAME, newName, roleId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> updateParentRole(long roleId, Long newParentRoleId) {
        return database.update(UPDATE_PARENT_ROLE, newParentRoleId, roleId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> updateSortOrder(long roleId, int newSortOrder) {
        return database.update(UPDATE_SORT_ORDER, newSortOrder, roleId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<ClanRole> findById(long id) {
        return database.query(SELECT_ROLE_BY_ID, id)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<Optional<ClanRole>> findByIdOptional(long id) {
        return database.query(SELECT_ROLE_BY_ID, id)
            .thenApply(result -> result.firstOptional()
                .map(mapper));
    }

    public CompletionStage<List<ClanRole>> findByClanId(long clanId) {
        return database.query(SELECT_ROLES_BY_CLAN_ID, clanId)
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }

    public CompletionStage<ClanRole> findByClanIdAndOwnerIsTrue(long clanId) {
        return database.query(SELECT_OWNER_ROLE_BY_CLAN_ID, clanId)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }
}
