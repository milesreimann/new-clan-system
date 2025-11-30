package io.github.milesreimann.clansystem.bungee.mapper;

import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import io.github.milesreimann.clansystem.bungee.entity.ClanPermissionImpl;

import java.util.function.Function;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public class ClanPermissionMapper implements Function<QueryRow, ClanPermission> {
    @Override
    public ClanPermission apply(QueryRow row) {
        return new ClanPermissionImpl(
            row.getOrThrow("id_permission", Long.class),
            ClanPermissionType.valueOf(row.getOrThrow("type", String.class)),
            row.getOrThrow("value", String.class)
        );
    }
}
