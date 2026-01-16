package io.github.milesreimann.clansystem.api.service;

import io.github.milesreimann.clansystem.api.entity.ClanInvitation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public interface ClanInvitationService {
    CompletionStage<Void> sendInvitation(long clanId, UUID sender, UUID receiver);

    CompletionStage<Void> acceptInvitation(long clanId, UUID player);

    CompletionStage<Void> declineInvitation(long clanId, UUID player);

    CompletionStage<Void> declineAllInvitations(UUID player);

    CompletionStage<ClanInvitation> getInvitationByClanIdForPlayer(long clanId, UUID player);

    CompletionStage<List<ClanInvitation>> listInvitationsForPlayer(UUID player);
}
