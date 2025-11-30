package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
@Data
public class ClanMemberImpl implements ClanMember {
    private final UUID uuid;
    private final Long clan;
    private final Long role;
    private final Instant joinedAt;
}
