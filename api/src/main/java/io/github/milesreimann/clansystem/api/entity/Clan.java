package io.github.milesreimann.clansystem.api.entity;

import java.util.UUID;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public interface Clan {
    Long getId();

    UUID getOwner();

    String getName();

    String getTag();

    Long getOwnerRole();

    Long getDefaultRole();
}