package io.github.milesreimann.clansystem.api.service;

import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public interface ClanPermissionService {
    CompletionStage<ClanPermission> getPermissionById(long permissionId);

    CompletionStage<ClanPermission> getPermissionByType(ClanPermissionType type);

    CompletionStage<List<ClanPermission>> listPermissionsByTypes(ClanPermissionType... types);

    CompletionStage<List<ClanPermission>> listAllPermissions();
}
