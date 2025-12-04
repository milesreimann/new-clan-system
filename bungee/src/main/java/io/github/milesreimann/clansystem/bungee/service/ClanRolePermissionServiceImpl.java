package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;
import io.github.milesreimann.clansystem.api.observer.ClanRoleDeleteObserver;
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
    private final ClanRolePermissionRepository repository;
    private final ClanPermissionService clanPermissionService;
    private final ClanRoleService clanRoleService;

    private final AsyncLoadingCache<Long, List<ClanRolePermission>> rolePermissionsCache;

    @Getter
    private final ClanRoleDeleteObserver clanRoleDeleteObserver;

    public ClanRolePermissionServiceImpl(
        ClanRolePermissionRepository repository,
        ClanPermissionService clanPermissionService,
        ClanRoleService clanRoleService
    ) {
        this.repository = repository;
        this.clanPermissionService = clanPermissionService;
        this.clanRoleService = clanRoleService;

        rolePermissionsCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .buildAsync((roleId, _) -> loadEffectivePermissions(roleId).toCompletableFuture());

        clanRoleDeleteObserver = new ClanRolePermissionCacheInvalidationListener(rolePermissionsCache);
    }

    @Override
    public CompletionStage<Void> addPermission(long roleId, long permissionId) {
        return repository.insert(roleId, permissionId)
            .thenCompose(_ -> clanRoleService.listRoleInheritanceHierarchy(roleId))
            .thenAccept(clanRoles -> {
                rolePermissionsCache.synchronous().refresh(roleId);
                clanRoles.forEach(clanRole -> rolePermissionsCache.synchronous().invalidate(clanRole.getId()));
            });
    }

    @Override
    public CompletionStage<Void> removePermission(long roleId, long permissionId) {
        return repository.deleteByRoleIdAndPermissionId(roleId, permissionId)
            .thenCompose(_ -> clanRoleService.listRoleInheritanceHierarchy(roleId))
            .thenAccept(clanRoles -> {
                rolePermissionsCache.synchronous().refresh(roleId);
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
                    .map(clanRole -> listPermissions(clanRole.getId()).toCompletableFuture())
                    .toList();

                return CompletableFuture.allOf(permissionFutures.toArray(new CompletableFuture[0]))
                    .thenApply(_ -> permissionFutures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .toList());
            });
    }
}
