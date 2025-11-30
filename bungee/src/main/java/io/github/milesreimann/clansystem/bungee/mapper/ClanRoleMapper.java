package io.github.milesreimann.clansystem.bungee.mapper;

import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import io.github.milesreimann.clansystem.bungee.entity.ClanRoleImpl;

import java.util.function.Function;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public class ClanRoleMapper implements Function<QueryRow, ClanRole> {
    @Override
    public ClanRole apply(QueryRow row) {
        return new ClanRoleImpl(
            row.getOrThrow("id_role", Long.class),
            row.get("clan_id", Long.class),
            row.getOrThrow("name", String.class),
            row.get("parent_role_id", Long.class),
            row.get("sort_order", Integer.class),
            row.getOrThrow("owner", Boolean.class)
        );
    }
}
