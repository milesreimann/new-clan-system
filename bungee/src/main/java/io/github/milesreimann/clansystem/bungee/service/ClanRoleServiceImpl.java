package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.bungee.observer.ClanDeleteObserver;
import io.github.milesreimann.clansystem.bungee.observer.ClanRoleDeleteObserver;
import io.github.milesreimann.clansystem.bungee.observer.ClanRoleInheritObserver;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public class ClanRoleServiceImpl implements ClanRoleService {
    private static final int CACHE_SIZE = 5_000;
    private static final int CACHE_EXPIRY_MINUTES = 10;

    private final ClanRoleRepository repository;

    private final Map<Long, Set<Long>> clanIdToRoleIdCache;
    private final AsyncLoadingCache<Long, Optional<ClanRole>> roleByIdCache;
    private final AsyncLoadingCache<Long, List<ClanRole>> rolesByClanCache;

    private final List<ClanRoleDeleteObserver> deleteObservers;
    private final List<ClanRoleInheritObserver> inheritObservers;
    @Getter
    private final ClanDeleteObserver clanDeleteObserver;

    public ClanRoleServiceImpl(ClanRoleRepository repository) {
        this.repository = repository;

        clanIdToRoleIdCache = new ConcurrentHashMap<>();

        roleByIdCache = Caffeine.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterWrite(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .removalListener(new ClanRoleCacheRemovalListener(clanIdToRoleIdCache))
            .buildAsync((clanRoleId, _) -> repository.findById(clanRoleId)
                .thenApply(Optional::ofNullable)
                .thenApply(optionalClanRole -> {
                    optionalClanRole.ifPresent(clanRole -> clanIdToRoleIdCache
                        .computeIfAbsent(clanRole.getClan(), _ -> ConcurrentHashMap.newKeySet())
                        .add(clanRole.getId())
                    );

                    return optionalClanRole;
                })
                .toCompletableFuture()
            );

        rolesByClanCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .buildAsync((clanId, _) -> repository.findByClanId(clanId).toCompletableFuture());

        deleteObservers = new CopyOnWriteArrayList<>();
        inheritObservers = new CopyOnWriteArrayList<>();

        clanDeleteObserver = new ClanRoleCacheInvalidationListener(
            clanIdToRoleIdCache,
            roleByIdCache,
            rolesByClanCache
        );
    }

    @Override
    public CompletionStage<Long> createRole(
        long clanId,
        String name,
        Long inheritsFrom,
        Integer sortOrder
    ) {
        return repository.insert(new ClanRoleImpl(null, clanId, name, inheritsFrom, sortOrder));
    }

    @Override
    public CompletionStage<Void> deleteRole(ClanRole role) {
        return repository.deleteById(role.getId())
            .thenRun(() -> {
                deleteObservers.forEach(observer -> observer.onClanRoleDeleted(role.getId()));
                roleByIdCache.synchronous().invalidate(role.getId());
                rolesByClanCache.synchronous().invalidate(role.getClan());
            });
    }

    @Override
    public CompletionStage<Void> inheritRole(ClanRole role, long inheritFrom) {
        return repository.updateInheritsFromId(role.getId(), inheritFrom)
            .thenRun(() -> {
                inheritObservers.forEach(observer -> observer.onClanRoleInherited(role.getId(), inheritFrom));
                roleByIdCache.synchronous().invalidate(role.getId());
                rolesByClanCache.synchronous().invalidate(role.getClan());
            });
    }

    @Override
    public CompletionStage<ClanRole> getRoleById(long roleId) {
        return roleByIdCache.get(roleId)
            .thenApply(optionalRole -> optionalRole.orElse(null));
    }

    @Override
    public CompletionStage<ClanRole> getRoleByClanIdAndName(long clanId, String name) {
        return repository.findByClanIdAndName(clanId, name);
    }

    @Override
    public CompletionStage<List<ClanRole>> listRolesByClanId(long clanId) {
        return rolesByClanCache.get(clanId);
    }

    @Override
    public CompletionStage<List<ClanRole>> listRoleInheritanceHierarchy(long roleId) {
        return repository.findInheritanceHierarchy(roleId);
    }

    @Override
    public CompletionStage<Boolean> isRoleHigher(long roleId, long targetRoleId) {
        if (roleId == targetRoleId) {
            return CompletableFuture.completedStage(false);
        }

        CompletionStage<List<ClanRole>> roleChainFuture = repository.findInheritanceHierarchy(roleId);
        CompletionStage<List<ClanRole>> targetChainFuture = repository.findInheritanceHierarchy(targetRoleId);

        return roleChainFuture
            .thenCombine(targetChainFuture, (roleChain, targetChain) -> {
                Set<Long> roleChainIds = roleChain.stream()
                    .map(ClanRole::getId)
                    .collect(Collectors.toSet());

                Set<Long> targetChainIds = targetChain.stream()
                    .map(ClanRole::getId)
                    .collect(Collectors.toSet());

                return !targetChainIds.contains(roleId) && roleChainIds.contains(targetRoleId);
            });
    }

    public void registerDeleteObserver(ClanRoleDeleteObserver observer) {
        deleteObservers.add(observer);
    }

    public void registerInheritObserver(ClanRoleInheritObserver observer) {
        inheritObservers.add(observer);
    }
}
