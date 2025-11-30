package io.github.milesreimann.clansystem.bungee.listener;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.github.milesreimann.clansystem.api.entity.ClanMember;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanMemberCacheRemovalListener implements RemovalListener<UUID, Optional<ClanMember>> {
    private final Map<Long, List<UUID>> clanIdToMemberUuidCache;

    @Override
    public void onRemoval(@Nullable UUID memberUuid, @Nullable Optional<ClanMember> optionalClanMember, RemovalCause cause) {
        if (cause != RemovalCause.EXPIRED || memberUuid == null) {
            return;
        }

        optionalClanMember.ifPresent(clanRole -> {
            List<UUID> memberUuids = clanIdToMemberUuidCache.get(clanRole.getClan());
            if (memberUuids == null) {
                return;
            }

            memberUuids.remove(memberUuid);
            if (memberUuids.isEmpty()) {
                clanIdToMemberUuidCache.remove(clanRole.getClan());
            }
        });
    }
}