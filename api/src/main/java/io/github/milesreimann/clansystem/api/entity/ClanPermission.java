package io.github.milesreimann.clansystem.api.entity;

import io.github.milesreimann.clansystem.api.model.ClanPermissionType;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public interface ClanPermission {
    Long getId();

    ClanPermissionType getType();

    String getValue();
}
