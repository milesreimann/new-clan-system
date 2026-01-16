package io.github.milesreimann.clansystem.bungee.service;

import io.github.milesreimann.clansystem.api.entity.ClanInvitation;
import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.bungee.entity.ClanInvitationImpl;
import io.github.milesreimann.clansystem.bungee.repository.ClanInvitationRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
@RequiredArgsConstructor
public class ClanInvitationServiceImpl implements ClanInvitationService {
    private final ClanInvitationRepository repository;
    private final ClanMemberService clanMemberService;

    @Override
    public CompletionStage<Void> sendInvitation(long clanId, UUID sender, UUID receiver) {
        return repository.insert(new ClanInvitationImpl(clanId, sender, receiver, null))
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> acceptInvitation(long clanId, UUID player) {
        return clanMemberService.joinClan(player, clanId)
            .thenCompose(_ -> repository.deleteByClanIdAndReceiver(clanId, player))
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> declineInvitation(long clanId, UUID player) {
        return repository.deleteByClanIdAndReceiver(clanId, player)
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> declineAllInvitations(UUID player) {
        return repository.deleteByReceiver(player)
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<ClanInvitation> getInvitationByClanIdForPlayer(long clanId, UUID player) {
        return repository.findByClanIdAndReceiver(clanId, player);
    }

    @Override
    public CompletionStage<List<ClanInvitation>> listInvitationsForPlayer(UUID player) {
        return repository.findByReceiver(player);
    }
}