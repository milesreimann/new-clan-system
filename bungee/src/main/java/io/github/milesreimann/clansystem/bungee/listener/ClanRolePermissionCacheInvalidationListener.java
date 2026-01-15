package io.github.milesreimann.clansystem.bungee.listener;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import io.github.milesreimann.clansystem.bungee.observer.ClanRoleDeleteObserver;
import io.github.milesreimann.clansystem.bungee.observer.ClanRoleInheritObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanRolePermissionCacheInvalidationListener implements ClanRoleDeleteObserver, ClanRoleInheritObserver {
    private final AsyncLoadingCache<Long, List<ClanRolePermission>> rolePermissionsCache;

    @Override
    public void onClanRoleDeleted(long roleId) {
        rolePermissionsCache.synchronous().invalidate(roleId);
    }

    @Override
    public void onClanRoleInherited(long roleId, long inheritsFromRoleId) {
        rolePermissionsCache.synchronous().invalidate(roleId);
    }
}
