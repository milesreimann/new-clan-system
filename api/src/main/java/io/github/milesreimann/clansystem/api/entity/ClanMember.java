package io.github.milesreimann.clansystem.api.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public interface ClanMember {
    UUID getUuid();

    Long getClan();

    Long getRole();

    Instant getJoinedAt();
}