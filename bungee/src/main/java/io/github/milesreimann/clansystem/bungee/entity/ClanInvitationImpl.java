package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.ClanInvitation;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 09.12.25
 */
@Data
public class ClanInvitationImpl implements ClanInvitation {
    private final Long clan;
    private final UUID sender;
    private final UUID recipient;
    private final Instant timestamp;
}
