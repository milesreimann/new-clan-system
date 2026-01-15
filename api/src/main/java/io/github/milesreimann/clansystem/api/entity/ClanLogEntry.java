package io.github.milesreimann.clansystem.api.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public interface ClanLogEntry {
    Long getId();

    Long getClan();

    String getType();

    UUID getActor();

    UUID getTarget();

    Instant getTimestamp();
}