package io.github.milesreimann.clansystem.bungee.service;

import io.github.milesreimann.clansystem.api.entity.ClanJoinRequest;
import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.bungee.entity.ClanJoinRequestImpl;
import io.github.milesreimann.clansystem.bungee.repository.ClanJoinRequestRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
@RequiredArgsConstructor
public class ClanJoinRequestServiceImpl implements ClanJoinRequestService {
    private final ClanJoinRequestRepository repository;
    private final ClanMemberService clanMemberService;

    @Override
    public CompletionStage<Void> sendJoinRequest(UUID player, long clanId) {
        return repository.insert(new ClanJoinRequestImpl(player, clanId, null))
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> acceptJoinRequest(UUID player, long clanId) {
        return clanMemberService.joinClan(player, clanId)
            .thenCompose(_ -> repository.deleteByClanId(clanId))
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> denyJoinRequest(UUID player, long clanId) {
        return repository.deleteByPlayerAndClanId(player, clanId)
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> denyAllJoinRequests(long clanId) {
        return repository.deleteByClanId(clanId)
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<ClanJoinRequest> getJoinRequestByPlayerForClan(UUID player, long clanId) {
        return repository.findByPlayerAndClanId(player, clanId);
    }

    @Override
    public CompletionStage<List<ClanJoinRequest>> listJoinRequestsForClan(long clanId) {
        return repository.findByClanId(clanId);
    }
}
