package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import lombok.Data;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@Data
public class ClanPermissionImpl implements ClanPermission {
    private final Long id;
    private final ClanPermissionType type;
    private final String value;
}
