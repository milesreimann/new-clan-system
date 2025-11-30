package io.github.milesreimann.clansystem.bungee.listener;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import io.github.milesreimann.clansystem.api.observer.ClanRoleDeleteObserver;
import io.github.milesreimann.clansystem.bungee.model.HasPermissionCacheKey;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanRolePermissionCacheInvalidationListener implements ClanRoleDeleteObserver {
    private final Map<Long, Set<HasPermissionCacheKey>> hasPermissionCacheKeys;
    private final AsyncLoadingCache<HasPermissionCacheKey, Boolean> hasPermissionCache;
    private final AsyncLoadingCache<Long, List<ClanRolePermission>> rolePermissionsByRoleIdCache;

    @Override
    public void onClanRoleDeleted(long roleId) {
        rolePermissionsByRoleIdCache.synchronous().invalidate(roleId);

        Set<HasPermissionCacheKey> cacheKeys = hasPermissionCacheKeys.remove(roleId);
        if (cacheKeys != null) {
            hasPermissionCache.synchronous().invalidateAll(cacheKeys);
        }
    }
}
