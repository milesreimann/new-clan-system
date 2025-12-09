package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanRoleMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
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
        name VARCHAR(32) NOT NULL COLLATE utf8mb4_general_ci,
        inherits_from_id BIGINT DEFAULT NULL,
        sort_order INT DEFAULT 0,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (clan_id) REFERENCES clans(id_clan) ON DELETE CASCADE,
        FOREIGN KEY (inherits_from_id) REFERENCES clan_roles(id_role) ON DELETE SET NULL,
        UNIQUE (clan_id, name),
        INDEX (clan_id),
        INDEX (inherits_from_id)
        );
        """;

    private static final String CREATE_DELETE_TRIGGER = """
        CREATE TRIGGER trg_clan_role_delete\s
        BEFORE DELETE ON clan_roles\s
        FOR EACH ROW\s
        BEGIN\s
            UPDATE clan_members cm\s
            INNER JOIN clans c ON c.id_clan = cm.clan_id\s
            SET cm.role_id = c.default_role_id\s
            WHERE cm.role_id = OLD.id_role;\s
        END;
        """;

    private static final String DROP_DELETE_TRIGGER = "DROP TRIGGER IF EXISTS trg_clan_role_delete";

    private static final String INSERT_ROLE = """
        INSERT INTO clan_roles(clan_id, name, inherits_from_id, sort_order)\s
        VALUES (?, ?, ?, ?);
        """;

    private static final String DELETE_ROLE = """
        DELETE\s
        FROM clan_roles\s
        WHERE id_role = ?;
        """;

    private static final String SELECT_ROLE_BY_ID = """
        SELECT id_role, clan_id, name, inherits_from_id, sort_order\s
        FROM clan_roles\s
        WHERE id_role = ?;
        """;

    private static final String SELECT_ROLES_BY_CLAN_ID = """
        SELECT id_role, clan_id, name, inherits_from_id, sort_order\s
        FROM clan_roles\s
        WHERE clan_id = NULL OR clan_id = ?;
        """;

    private static final String FIND_INHERITANCE_HIERARCHY = """
        WITH RECURSIVE inherits_hierarchy AS (
            SELECT id_role, clan_id, name, inherits_from_id, sort_order
            FROM clan_roles
            WHERE id_role = ?
            UNION ALL
            SELECT cr.id_role, cr.clan_id, cr.name, cr.inherits_from_id, cr.sort_order
            FROM clan_roles cr
            INNER JOIN inherits_hierarchy ih ON ih.inherits_from_id = cr.id_role
        )
        SELECT id_role, clan_id, name, inherits_from_id, sort_order
        FROM inherits_hierarchy;
        """;

    private static final String SELECT_ROLE_BY_CLAN_ID_AND_NAME = """
        SELECT id_role, clan_id, name, inherits_from_id, sort_order\s
        FROM clan_roles\s
        WHERE clan_id = ? AND name = ?;
        """;

    private static final String UPDATE_INHERITS_FROM_ID = """
        UPDATE clan_roles\s
        SET inherits_from_id = ?\s
        WHERE id_role = ?;
        """;

    private static final String FIND_DEFAULT_ROLE_BY_CLAN_ID = """
        SELECT id_role, clan_id, name, inherits_from_id, sort_order\s
        FROM clan_roles cr\s
        INNER JOIN clans c\s
        ON c.default_role_id = cr.id_role
        WHERE clan_id = ?;
        """;

    private final MySQLDatabase database;
    private final ClanRoleMapper mapper;

    public void createTableAndTrigger() {
        database.update(CREATE_TABLE).join();
        database.update(DROP_DELETE_TRIGGER).join();
        database.update(CREATE_DELETE_TRIGGER).join();
    }

    public CompletionStage<Long> insert(ClanRole role) {
        return database.insert(INSERT_ROLE, role.getClan(), role.getName(), role.getInheritsFromRole().orElse(null), role.getSortOrder().orElse(null));
    }

    public CompletionStage<Boolean> deleteById(long id) {
        return database.update(DELETE_ROLE, id)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<ClanRole> findById(long id) {
        return database.query(SELECT_ROLE_BY_ID, id)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<List<ClanRole>> findByClanId(long clanId) {
        return database.query(SELECT_ROLES_BY_CLAN_ID, clanId)
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }

    public CompletionStage<List<ClanRole>> findInheritanceHierarchy(long roleId) {
        return database.query(FIND_INHERITANCE_HIERARCHY, roleId)
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }

    public CompletionStage<ClanRole> findByClanIdAndName(long clanId, String name) {
        return database.query(SELECT_ROLE_BY_CLAN_ID_AND_NAME, clanId, name)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<Boolean> updateInheritsFromId(long roleId, Long inheritsFromId) {
        return database.update(UPDATE_INHERITS_FROM_ID, inheritsFromId, roleId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<ClanRole> findDefaultRoleByClanId(long clanId) {
        return database.query(FIND_DEFAULT_ROLE_BY_CLAN_ID, clanId)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }
}
