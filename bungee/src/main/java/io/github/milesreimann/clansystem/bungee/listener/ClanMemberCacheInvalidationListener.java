package io.github.milesreimann.clansystem.bungee.listener;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.observer.ClanDeleteObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanMemberCacheInvalidationListener implements ClanDeleteObserver {
    private final Map<Long, List<UUID>> clanIdToMemberUuidCache;
    private final AsyncLoadingCache<UUID, Optional<ClanMember>> memberByUuidCache;
    private final AsyncLoadingCache<Long, List<ClanMember>> membersByClanCache;

    @Override
    public void onClanDeleted(long clanId) {
        List<UUID> memberUuids = clanIdToMemberUuidCache.remove(clanId);
        if (memberUuids != null) {
            memberByUuidCache.synchronous().invalidateAll(memberUuids);
        }

        membersByClanCache.synchronous().invalidate(clanId);
    }
}