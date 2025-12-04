package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.bungee.repository.ClanPermissionRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public class ClanPermissionServiceImpl implements ClanPermissionService {
    private final ClanPermissionRepository repository;
    private final AsyncLoadingCache<Long, Optional<ClanPermission>> permissionByIdCache;

    public ClanPermissionServiceImpl(ClanPermissionRepository repository) {
        this.repository = repository;

        permissionByIdCache = Caffeine.newBuilder()
            .maximumSize(100L)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .buildAsync((permissionId, _) -> repository.findById(permissionId)
                .thenApply(Optional::ofNullable)
                .toCompletableFuture());
    }

    @Override
    public CompletionStage<ClanPermission> getPermissionById(long permissionId) {
        return permissionByIdCache.get(permissionId)
            .thenApply(optionalPermission -> optionalPermission.orElse(null));
    }

    @Override
    public CompletionStage<ClanPermission> getPermissionByType(ClanPermissionType type) {
        return repository.findByType(type);
    }

    @Override
    public CompletionStage<List<ClanPermission>> listPermissionsByTypes(ClanPermissionType... types) {
        return null;
    }

    @Override
    public CompletionStage<List<ClanPermission>> listAllPermissions() {
        return repository.findAll();
    }
}
