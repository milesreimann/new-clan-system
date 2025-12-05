package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanRolePermissionMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanRolePermissionRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS clan_role_permissions(
        role_id BIGINT NOT NULL,
        permission_id BIGINT NOT NULL,
        added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (role_id, permission_id),
        FOREIGN KEY (role_id) REFERENCES clan_roles(id_role) ON DELETE CASCADE,
        FOREIGN KEY (permission_id) REFERENCES clan_permissions(id_permission) ON DELETE CASCADE,
        INDEX (role_id),
        INDEX (permission_id)
        );
        """;

    private static final String INSERT_PERMISSION = """
        INSERT INTO clan_role_permissions(role_id, permission_id)
        VALUES (?, ?);
        """;

    private static final String DELETE_PERMISSION = """
        DELETE\s
        FROM clan_role_permissions\s
        WHERE role_id = ? AND permission_id = ?;
        """;

    private static final String SELECT_PERMISSIONS_BY_ROLE_ID = """
        SELECT role_id, permission_id\s
        FROM clan_role_permissions\s
        WHERE role_id = ?;
        """;

    private final MySQLDatabase database;
    private final ClanRolePermissionMapper mapper;

    public void createTable() {
        database.update(CREATE_TABLE).join();
    }

    public CompletionStage<Boolean> insert(long roleId, long permissionId) {
        return database.update(INSERT_PERMISSION, roleId, permissionId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<Boolean> deleteByRoleIdAndPermissionId(long roleId, long permissionId) {
        return database.update(DELETE_PERMISSION, roleId, permissionId)
            .thenApply(rows -> rows == 1);
    }

    public CompletionStage<List<ClanRolePermission>> findByRoleId(long roleId) {
        return database.query(SELECT_PERMISSIONS_BY_ROLE_ID, roleId)
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }
}
