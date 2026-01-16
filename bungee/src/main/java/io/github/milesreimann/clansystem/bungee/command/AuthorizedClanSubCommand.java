package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 16.01.2026
 */
public abstract class AuthorizedClanSubCommand extends ClanSubCommand {
    protected final ClanMemberService clanMemberService;
    protected final ClanPermissionService clanPermissionService;
    protected final ClanRolePermissionService clanRolePermissionService;

    public AuthorizedClanSubCommand(ClanSystemPlugin plugin) {
        clanMemberService = plugin.getClanMemberService();
        clanPermissionService = plugin.getClanPermissionService();
        clanRolePermissionService = plugin.getClanRolePermissionService();
    }

    protected CompletionStage<ClanMember> loadExecutorWithPermissions(
        UUID playerUuid,
        ClanPermissionType permissionType
    ) {
        return clanMemberService.getMemberByUuid(playerUuid)
            .thenCompose(this::validateExecutorInClan)
            .thenCompose(executor -> validatePermission(executor, permissionType));
    }

    protected CompletionStage<Boolean> checkPermission(ClanMember member, ClanPermission permission) {
        if (permission == null) {
            return failWithMessage("berechtigung existiert nicht. kontaktiere einen admin");
        }

        return clanRolePermissionService.hasPermission(member.getRole(), permission.getId())
            .thenCompose(hasPermission -> {
                if (!Boolean.TRUE.equals(hasPermission)) {
                    return failWithMessage("keine rechte");
                }

                return CompletableFuture.completedFuture(true);
            });
    }

    private CompletionStage<ClanMember> validateExecutorInClan(ClanMember executor) {
        if (executor == null) {
            return failWithMessage("du bist nicht in einem clan");
        }

        return CompletableFuture.completedFuture(executor);
    }

    private CompletionStage<ClanMember> validatePermission(ClanMember executor, ClanPermissionType permissionType) {
        return clanPermissionService.getPermissionByType(permissionType)
            .thenCompose(permission -> checkPermission(executor, permission))
            .thenApply(_ -> executor);
    }
}
