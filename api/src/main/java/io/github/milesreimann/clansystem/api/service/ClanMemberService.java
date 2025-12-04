package io.github.milesreimann.clansystem.api.service;

import io.github.milesreimann.clansystem.api.entity.ClanMember;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public interface ClanMemberService {
    CompletionStage<Void> joinClan(UUID uuid, long clanId, long roleId);

    CompletionStage<Void> leaveClan(ClanMember member);

    CompletionStage<Void> updateRole(ClanMember member, long newRoleId);

    CompletionStage<Void> sendMessage(ClanMember sender, ClanMember receiver, String message);

    CompletionStage<ClanMember> getMemberByUuid(UUID memberUuid);

    CompletionStage<List<ClanMember>> listMembersByClanId(long clanId);

    CompletionStage<Boolean> isInClan(UUID memberUuid);

    CompletionStage<Boolean> isInClan(UUID memberUuid, long clanId);
}
