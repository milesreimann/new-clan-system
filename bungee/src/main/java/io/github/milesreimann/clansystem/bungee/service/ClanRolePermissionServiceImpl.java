package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import io.github.milesreimann.clansystem.api.observer.ClanRoleDeleteObserver;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.bungee.listener.ClanRolePermissionCacheInvalidationListener;
import io.github.milesreimann.clansystem.bungee.listener.ClanRolePermissionCacheRemovalListener;
import io.github.milesreimann.clansystem.bungee.model.HasPermissionCacheKey;
import io.github.milesreimann.clansystem.bungee.repository.ClanRolePermissionRepository;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public class ClanRolePermissionServiceImpl implements ClanRolePermissionService {
    private final ClanRolePermissionRepository repository;
    private final ClanPermissionService clanPermissionService;
    private final Map<Long, Set<HasPermissionCacheKey>> hasPermissionCacheKeys;
    private final AsyncLoadingCache<HasPermissionCacheKey, Boolean> hasPermissionCache;
    private final AsyncLoadingCache<Long, List<ClanRolePermission>> rolePermissionsByRoleIdCache;
    @Getter
    private final ClanRoleDeleteObserver clanRoleDeleteObserver;

    public ClanRolePermissionServiceImpl(
        ClanRolePermissionRepository repository,
        ClanPermissionService clanPermissionService
    ) {
        this.repository = repository;
        this.clanPermissionService = clanPermissionService;

        hasPermissionCacheKeys = new ConcurrentHashMap<>();

        hasPermissionCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .removalListener(new ClanRolePermissionCacheRemovalListener(hasPermissionCacheKeys))
            .buildAsync((key, _) -> repository.existsByRoleIdAndPermissionId(key.role(), key.permission())
                .thenApply(Boolean.TRUE::equals)
                .thenApply(exists -> {
                    hasPermissionCacheKeys
                        .computeIfAbsent(key.role(), _ -> ConcurrentHashMap.newKeySet())
                        .add(key);
                    return exists;
                })
                .toCompletableFuture());

        rolePermissionsByRoleIdCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .buildAsync((roleId, _) -> repository.findByRoleId(roleId).toCompletableFuture());

        clanRoleDeleteObserver = new ClanRolePermissionCacheInvalidationListener(
            hasPermissionCacheKeys,
            hasPermissionCache,
            rolePermissionsByRoleIdCache
        );
    }

    @Override
    public CompletionStage<Void> addPermission(long roleId, long permissionId) {
        return repository.insert(roleId, permissionId)
            .thenRun(() -> hasPermissionCache.synchronous().invalidate(new HasPermissionCacheKey(roleId, permissionId)));
    }

    @Override
    public CompletionStage<Void> removePermission(long roleId, long permissionId) {
        return repository.deleteByRoleIdAndPermissionId(roleId, permissionId)
            .thenRun(() -> hasPermissionCache.synchronous().invalidate(new HasPermissionCacheKey(roleId, permissionId)));
    }

    @Override
    public CompletionStage<Boolean> hasPermission(long roleId, long permissionId) {
        CompletionStage<List<ClanRolePermission>> rolePermissionsByRoleId = rolePermissionsByRoleIdCache.getIfPresent(roleId);
        if (rolePermissionsByRoleId != null) {
            return rolePermissionsByRoleId.thenApply(clanRolePermissions -> clanRolePermissions.stream()
                .anyMatch(clanRolePermission -> clanRolePermission.getPermission().equals(permissionId)));
        }

        return hasPermissionCache.get(new HasPermissionCacheKey(roleId, permissionId));
    }

    @Override
    public CompletionStage<List<ClanRolePermission>> listPermissions(long roleId) {
        return rolePermissionsByRoleIdCache.get(roleId);
    }

    @Override
    public CompletionStage<Void> grantAllPermissions(long roleId) {
        return clanPermissionService.listPermissions()
            .thenCompose(permissions -> {
                List<CompletableFuture<Void>> futures = permissions.stream()
                    .map(p -> addPermission(roleId, p.getId()).toCompletableFuture())
                    .toList();

                return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
            });
    }
}
