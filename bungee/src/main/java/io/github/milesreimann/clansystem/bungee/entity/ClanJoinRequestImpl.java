package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.ClanJoinRequest;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 09.12.25
 */
@Data
public class ClanJoinRequestImpl implements ClanJoinRequest {
    private final UUID player;
    private final long clan;
    private final Instant timestamp;
}
