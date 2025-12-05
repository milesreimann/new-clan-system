package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.Clan;
import lombok.Data;

import java.util.UUID;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
@Data
public class ClanImpl implements Clan {
    private final Long id;
    private final UUID owner;
    private final String name;
    private final String tag;
    private final Long ownerRole;
    private final Long defaultRole;
}
