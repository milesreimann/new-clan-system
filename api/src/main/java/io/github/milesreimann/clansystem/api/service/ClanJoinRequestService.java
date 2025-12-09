package io.github.milesreimann.clansystem.api.service;

import io.github.milesreimann.clansystem.api.entity.ClanJoinRequest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public interface ClanJoinRequestService {
    CompletionStage<Void> sendJoinRequest(UUID player, long clanId);

    CompletionStage<Void> acceptJoinRequest(UUID player, long clanId);

    CompletionStage<Void> denyJoinRequest(UUID player, long clanId);

    CompletionStage<Void> denyAllJoinRequests(long clanId);

    CompletionStage<ClanJoinRequest> getJoinRequestByPlayerForClan(UUID player, long clanId);

    CompletionStage<List<ClanJoinRequest>> listJoinRequestsForClan(long clanId);
}
