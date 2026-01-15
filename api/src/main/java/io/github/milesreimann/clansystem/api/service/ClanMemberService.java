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

    CompletionStage<Void> joinClan(UUID uuid, long clanId);

    CompletionStage<Void> leaveClan(ClanMember member);

    CompletionStage<Void> updateRole(ClanMember member, long newRoleId);

    CompletionStage<ClanMember> getMemberByUuid(UUID memberUuid);

    CompletionStage<Boolean> isInClan(UUID memberUuid);
}
