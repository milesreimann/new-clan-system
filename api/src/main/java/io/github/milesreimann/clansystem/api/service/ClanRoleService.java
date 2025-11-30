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
    CompletionStage<ClanRole> createRole(long clanId, String name, Long parentRole, Integer sortOrder, boolean owner);

    CompletionStage<Void> deleteRole(ClanRole role);

    CompletionStage<Void> updateName(long roleId, String newName);

    CompletionStage<Void> updateParentRole(long roleId, Long newParentRoleId);

    CompletionStage<Void> updateSortOrder(long roleId, Integer newSortOrder);

    CompletionStage<ClanRole> getRoleById(long roleId);

    CompletionStage<ClanRole> getOwnerRoleByClanId(long clanId);

    CompletionStage<List<ClanRole>> listRolesByClanId(long clanId);

    void registerDeleteObserver(ClanRoleDeleteObserver observer);
}
