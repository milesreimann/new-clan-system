package io.github.milesreimann.clansystem.api.service;

import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.api.observer.ClanRoleDeleteObserver;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public interface ClanRoleService {
    CompletionStage<Long> createRole(long clanId, String name, Long inheritFrom, Integer sortOrder);

    CompletionStage<Void> deleteRole(ClanRole role);

    CompletionStage<Void> inheritRole(long roleId, long inheritFrom);

    CompletionStage<ClanRole> getRoleById(long roleId);

    CompletionStage<ClanRole> getRoleByClanIdAndName(long clanId, String name);

    CompletionStage<List<ClanRole>> listRolesByClanId(long clanId);

    CompletionStage<List<ClanRole>> listRoleInheritanceHierarchy(long roleId);

    CompletionStage<Boolean> isRoleHigher(long roleId, long targetRoleId);

    void registerDeleteObserver(ClanRoleDeleteObserver observer);
}
