package io.github.milesreimann.clansystem.api.service;

import io.github.milesreimann.clansystem.api.entity.ClanRolePermission;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public interface ClanRolePermissionService {
    CompletionStage<Void> addPermission(long roleId, long permissionId);

    CompletionStage<Void> removePermission(long roleId, long permissionId);

    CompletionStage<Boolean> hasPermission(long roleId, long permissionId);

    CompletionStage<List<ClanRolePermission>> listPermissions(long roleId);

    CompletionStage<Void> grantAllPermissions(long roleId);
}
