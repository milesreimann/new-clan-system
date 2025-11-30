package io.github.milesreimann.clansystem.bungee.mapper;

import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import io.github.milesreimann.clansystem.bungee.entity.ClanRolePermissionImpl;

import java.util.function.Function;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public class ClanRolePermissionMapper implements Function<QueryRow, ClanRolePermission> {
    @Override
    public ClanRolePermission apply(QueryRow row) {
        return new ClanRolePermissionImpl(
            row.getOrThrow("role_id", Long.class),
            row.getOrThrow("permission_id", Long.class)
        );
    }
}
