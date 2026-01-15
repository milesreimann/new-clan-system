package io.github.milesreimann.clansystem.bungee.listener;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.github.milesreimann.clansystem.api.entity.ClanRole;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanRoleCacheRemovalListener implements RemovalListener<Long, Optional<ClanRole>> {
    private final Map<Long, Set<Long>> clanIdToRoleIdCache;

    @Override
    public void onRemoval(
        @Nullable Long roleId,
        @Nullable Optional<ClanRole> optionalClanRole,
        RemovalCause cause
    ) {
        if (cause != RemovalCause.EXPIRED || roleId == null) {
            return;
        }

        optionalClanRole.ifPresent(clanRole -> {
            Set<Long> roleIds = clanIdToRoleIdCache.get(clanRole.getClan());
            if (roleIds == null) {
                return;
            }

            roleIds.remove(roleId);
            if (roleIds.isEmpty()) {
                clanIdToRoleIdCache.remove(clanRole.getClan());
            }
        });
    }
}
