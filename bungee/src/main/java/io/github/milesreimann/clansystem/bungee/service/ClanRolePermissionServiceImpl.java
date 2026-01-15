package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import io.github.milesreimann.clansystem.bungee.observer.ClanRoleDeleteObserver;
import io.github.milesreimann.clansystem.bungee.observer.ClanRoleInheritObserver;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.bungee.listener.ClanRolePermissionCacheInvalidationListener;
import io.github.milesreimann.clansystem.bungee.repository.ClanRolePermissionRepository;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public class ClanRolePermissionServiceImpl implements ClanRolePermissionService {
    private static final int CACHE_SIZE = 5_000;
    private static final int CACHE_EXPIRY_MINUTES = 15;

    private final ClanRolePermissionRepository repository;
    private final ClanPermissionService clanPermissionService;
    private final ClanRoleService clanRoleService;

    private final AsyncLoadingCache<Long, List<ClanRolePermission>> rolePermissionsCache;

    @Getter
    private final ClanRoleDeleteObserver clanRoleDeleteObserver;
    @Getter
    private final ClanRoleInheritObserver clanRoleInheritObserver;

    public ClanRolePermissionServiceImpl(
        ClanRolePermissionRepository repository,
        ClanPermissionService clanPermissionService,
        ClanRoleService clanRoleService
    ) {
        this.repository = repository;
        this.clanPermissionService = clanPermissionService;
        this.clanRoleService = clanRoleService;

        rolePermissionsCache = Caffeine.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterWrite(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .buildAsync((roleId, _) -> loadEffectivePermissions(roleId).toCompletableFuture());

        ClanRolePermissionCacheInvalidationListener listener = new ClanRolePermissionCacheInvalidationListener(rolePermissionsCache);
        clanRoleDeleteObserver = listener;
        clanRoleInheritObserver = listener;
    }

    @Override
    public CompletionStage<Void> addPermission(long roleId, long permissionId) {
        return repository.insert(roleId, permissionId)
            .thenCompose(_ -> clanRoleService.listRoleInheritanceHierarchy(roleId))
            .thenAccept(clanRoles -> {
                rolePermissionsCache.synchronous().invalidate(roleId);
                clanRoles.forEach(clanRole -> rolePermissionsCache.synchronous().invalidate(clanRole.getId()));
            });
    }

    @Override
    public CompletionStage<Void> removePermission(long roleId, long permissionId) {
        return repository.deleteByRoleIdAndPermissionId(roleId, permissionId)
            .thenCompose(_ -> clanRoleService.listRoleInheritanceHierarchy(roleId))
            .thenAccept(clanRoles -> {
                rolePermissionsCache.synchronous().invalidate(roleId);
                clanRoles.forEach(clanRole -> rolePermissionsCache.synchronous().invalidate(clanRole.getId()));
            });
    }

    @Override
    public CompletionStage<Boolean> hasPermission(long roleId, long permissionId) {
        return rolePermissionsCache.get(roleId)
            .thenApply(permissions -> permissions.stream()
                .anyMatch(clanRolePermission -> clanRolePermission.getPermission() == permissionId));
    }

    @Override
    public CompletionStage<Boolean> hasAnyPermission(long roleId, long... permissionIds) {
        Set<Long> permissionSet = Arrays.stream(permissionIds)
            .boxed()
            .collect(Collectors.toSet());

        return rolePermissionsCache.get(roleId)
            .thenApply(permissions -> permissions.stream()
                .map(ClanRolePermission::getPermission)
                .anyMatch(permissionSet::contains));
    }

    @Override
    public CompletionStage<List<ClanRolePermission>> listPermissions(long roleId) {
        return rolePermissionsCache.get(roleId);
    }

    @Override
    public CompletionStage<Void> grantAllPermissions(long roleId) {
        return clanPermissionService.listAllPermissions()
            .thenCompose(permissions -> {
                List<CompletableFuture<Void>> futures = permissions.stream()
                    .map(p -> addPermission(roleId, p.getId()).toCompletableFuture())
                    .toList();

                return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
            });
    }

    private CompletionStage<List<ClanRolePermission>> loadEffectivePermissions(long roleId) {
        return clanRoleService.listRoleInheritanceHierarchy(roleId)
            .thenCompose(clanRoles -> {
                List<CompletableFuture<List<ClanRolePermission>>> permissionFutures = clanRoles.stream()
                    .map(clanRole -> repository.findByRoleId(clanRole.getId()).toCompletableFuture())
                    .toList();

                return CompletableFuture.allOf(permissionFutures.toArray(CompletableFuture[]::new))
                    .thenApply(_ -> permissionFutures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .toList());
            });
    }
}
