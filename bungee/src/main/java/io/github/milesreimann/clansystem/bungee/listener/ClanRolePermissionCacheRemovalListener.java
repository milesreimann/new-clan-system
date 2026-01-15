package io.github.milesreimann.clansystem.bungee.listener;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.github.milesreimann.clansystem.bungee.model.HasPermissionCacheKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanRolePermissionCacheRemovalListener implements RemovalListener<HasPermissionCacheKey, Boolean> {
    private final Map<Long, Set<HasPermissionCacheKey>> hasPermissionCacheKeys;

    @Override
    public void onRemoval(
        @Nullable HasPermissionCacheKey key,
        @Nullable Boolean hasPermission,
        RemovalCause cause
    ) {
        if (cause != RemovalCause.EXPIRED || key == null) {
            return;
        }

        long roleId = key.role();
        Set<HasPermissionCacheKey> cacheKeys = hasPermissionCacheKeys.get(roleId);

        if (cacheKeys == null) {
            return;
        }

        cacheKeys.remove(key);
        if (cacheKeys.isEmpty()) {
            hasPermissionCacheKeys.remove(roleId);
        }
    }
}