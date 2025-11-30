package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.api.observer.ClanDeleteObserver;
import io.github.milesreimann.clansystem.api.observer.ClanRoleDeleteObserver;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.bungee.entity.ClanRoleImpl;
import io.github.milesreimann.clansystem.bungee.listener.ClanRoleCacheInvalidationListener;
import io.github.milesreimann.clansystem.bungee.listener.ClanRoleCacheRemovalListener;
import io.github.milesreimann.clansystem.bungee.repository.ClanRoleRepository;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public class ClanRoleServiceImpl implements ClanRoleService {
    private final ClanRoleRepository repository;
    private final Map<Long, Set<Long>> clanIdToRoleIdCache;
    private final AsyncLoadingCache<Long, Optional<ClanRole>> roleByIdCache;
    private final AsyncLoadingCache<Long, List<ClanRole>> rolesByClanCache;
    private final List<ClanRoleDeleteObserver> deleteObservers;
    @Getter
    private ClanDeleteObserver clanDeleteObserver;

    public ClanRoleServiceImpl(ClanRoleRepository repository) {
        this.repository = repository;

        clanIdToRoleIdCache = new ConcurrentHashMap<>();

        roleByIdCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(new ClanRoleCacheRemovalListener(clanIdToRoleIdCache))
            .buildAsync((clanRoleId, _) -> repository.findById(clanRoleId)
                .thenApply(Optional::ofNullable)
                .thenApply(optionalClanRole -> {
                    optionalClanRole.ifPresent(clanRole -> clanIdToRoleIdCache
                        .computeIfAbsent(clanRole.getClan(), _ -> ConcurrentHashMap.newKeySet())
                        .add(clanRole.getId()));
                    return optionalClanRole;
                })
                .toCompletableFuture());

        rolesByClanCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .buildAsync((clanId, _) -> repository.findByClanId(clanId).toCompletableFuture());

        deleteObservers = new CopyOnWriteArrayList<>();

        clanDeleteObserver = new ClanRoleCacheInvalidationListener(
            clanIdToRoleIdCache,
            roleByIdCache,
            rolesByClanCache
        );
    }

    @Override
    public CompletionStage<ClanRole> createRole(
        long clanId,
        String name,
        Long parentRole,
        Integer sortOrder,
        boolean owner
    ) {
        return repository.insert(new ClanRoleImpl(null, clanId, name, parentRole, sortOrder, owner));
    }

    @Override
    public CompletionStage<Void> deleteRole(ClanRole role) {
        return repository.deleteById(role.getId())
            .thenRun(() -> deleteObservers.forEach(observer -> observer.onClanRoleDeleted(role.getId())));
    }

    @Override
    public CompletionStage<Void> updateName(long roleId, String newName) {
        return repository.updateName(roleId, newName)
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> updateParentRole(long roleId, Long newParentRoleId) {
        return repository.updateParentRole(roleId, newParentRoleId)
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> updateSortOrder(long roleId, Integer newSortOrder) {
        return repository.updateSortOrder(roleId, newSortOrder)
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<ClanRole> getRoleById(long roleId) {
        return roleByIdCache.get(roleId)
            .thenApply(optionalRole -> optionalRole.orElse(null));
    }

    @Override
    public CompletionStage<ClanRole> getOwnerRoleByClanId(long clanId) {
        return repository.findByClanIdAndOwnerIsTrue(clanId);
    }

    @Override
    public CompletionStage<List<ClanRole>> listRolesByClanId(long clanId) {
        return rolesByClanCache.get(clanId);
    }

    @Override
    public void registerDeleteObserver(ClanRoleDeleteObserver observer) {
        deleteObservers.add(observer);
    }
}
