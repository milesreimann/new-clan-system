package io.github.milesreimann.clansystem.bungee.listener;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.api.observer.ClanDeleteObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanRoleCacheInvalidationListener implements ClanDeleteObserver {
    private final Map<Long, Set<Long>> clanIdToRoleIdCache;
    private final AsyncLoadingCache<Long, Optional<ClanRole>> roleByIdCache;
    private final AsyncLoadingCache<Long, List<ClanRole>> rolesByClanCache;

    @Override
    public void onClanDeleted(long clanId) {
        Set<Long> roleIds = clanIdToRoleIdCache.remove(clanId);
        if (roleIds != null) {
            roleByIdCache.synchronous().invalidateAll(roleIds);
        }

        rolesByClanCache.synchronous().invalidate(clanId);
    }
}