package io.github.milesreimann.clansystem.api.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 04.12.25
 */
public interface ClanJoinRequest {
    UUID getPlayer();

    Long getClan();

    Instant getTimestamp();
}
