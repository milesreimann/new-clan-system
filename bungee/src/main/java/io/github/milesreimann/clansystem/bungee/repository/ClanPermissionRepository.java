package io.github.milesreimann.clansystem.bungee.repository;

import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanPermissionMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanPermissionRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS clan_permissions(
        id_permission BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        type VARCHAR(64) NOT NULL,
        value VARCHAR(64) NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(type)
        );
        """;

    private static final String INSERT_PERMISSION = """
        INSERT IGNORE INTO clan_permissions(type, value)\s
        VALUES (?, ?);
        """;

    private static final String SELECT_PERMISSION_BY_ID = """
        SELECT id_permission, type, value\s
        FROM clan_permissions\s
        WHERE id_permission = ?;
        """;

    private static final String SELECT_PERMISSION_BY_TYPE = """
        SELECT id_permission, type, value\s
        FROM clan_permissions\s
        WHERE type = ?;
        """;

    private static final String SELECT_PERMISSIONS = """
        SELECT id_permission, type, value\s
        FROM clan_permissions;
        """;

    private final MySQLDatabase database;
    private final ClanPermissionMapper mapper;

    public void createTableAndPermissions() {
        database.update(CREATE_TABLE).join();

        for (ClanPermissionType value : ClanPermissionType.values()) {
            database.insert(INSERT_PERMISSION, value.name(), value.getPermission());
        }
    }

    public CompletionStage<ClanPermission> findById(long permissionId) {
        return database.query(SELECT_PERMISSION_BY_ID, permissionId)
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<ClanPermission> findByType(ClanPermissionType type) {
        return database.query(SELECT_PERMISSION_BY_TYPE, type.name())
            .thenApply(result -> result.firstOptional()
                .map(mapper)
                .orElse(null));
    }

    public CompletionStage<List<ClanPermission>> findAll() {
        return database.query(SELECT_PERMISSIONS)
            .thenApply(result -> result.stream()
                .map(mapper)
                .toList());
    }
}