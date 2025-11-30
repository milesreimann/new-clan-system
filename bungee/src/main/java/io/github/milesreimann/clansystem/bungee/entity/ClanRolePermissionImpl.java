package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import lombok.Data;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@Data
public class ClanRolePermissionImpl implements ClanRolePermission {
    private final Long role;
    private final Long permission;
}
