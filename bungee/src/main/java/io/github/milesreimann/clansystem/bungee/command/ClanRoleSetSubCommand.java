package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 04.12.25
 */
public class ClanRoleSetSubCommand extends ClanRoleCommand {
    private final ClanRoleService clanRoleService;
    private final ClanMemberService clanMemberService;
    private final ClanPermissionService clanPermissionService;
    private final ClanRolePermissionService clanRolePermissionService;

    public ClanRoleSetSubCommand(ClanSystemPlugin plugin) {
        super(ClanPermissionType.SET_ROLE);
        clanRoleService = plugin.getClanRoleService();
        clanMemberService = plugin.getClanMemberService();
        clanPermissionService = plugin.getClanPermissionService();
        clanRolePermissionService = plugin.getClanRolePermissionService();
    }

    @Override
    public CompletionStage<Void> execute(ProxiedPlayer player, ClanMember clanMember, String[] args) {
        if (args.length < 2) {
            // help
            return CompletableFuture.completedStage(null);
        }

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("ungültige uuid");
            return CompletableFuture.completedStage(null);
        }

        String roleName = args[1];

        if (targetUuid.equals(clanMember.getUuid())) {
            player.sendMessage("du kannst deine eigene rolle nicht setzen");
            return CompletableFuture.completedStage(null);
        }

        return processSetRole(player, clanMember, targetUuid, roleName)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processSetRole(
        ProxiedPlayer player,
        ClanMember executor,
        UUID targetUuid,
        String roleName
    ) {
        return loadAndValidateTarget(executor, targetUuid)
            .thenCompose(target -> validateTargetRoleEditable(executor, target))
            .thenCompose(this::validateSetRoleBypassProtection)
            .thenCompose(target -> updateRole(player, target, roleName));
    }

    private CompletionStage<ClanMember> loadAndValidateTarget(ClanMember executor, UUID targetUuid) {
        long clanId = executor.getClan();

        return clanMemberService.getMemberByUuid(targetUuid)
            .thenCompose(target -> {
                if (target == null || target.getClan() != clanId) {
                    return failWithMessage("spieler ist nicht in deinem clan");
                }

                return CompletableFuture.completedFuture(target);
            });
    }

    private CompletionStage<ClanMember> validateTargetRoleEditable(ClanMember executor, ClanMember target) {
        return clanRoleService.isRoleHigher(target.getRole(), executor.getRole())
            .thenCompose(isTargetRoleHigher -> {
                if (Boolean.TRUE.equals(isTargetRoleHigher)) {
                    return failWithMessage("du kannst die rolle dieses spielers nicht anpassen");
                }

                return CompletableFuture.completedFuture(target);
            });
    }

    private CompletionStage<ClanMember> validateSetRoleBypassProtection(ClanMember target) {
        return clanPermissionService.getPermissionByType(ClanPermissionType.SET_ROLE_BYPASS)
            .thenCompose(bypassPermission -> {
                if (bypassPermission == null) {
                    return CompletableFuture.completedFuture(target);
                }

                return clanRolePermissionService.hasPermission(target.getRole(), bypassPermission.getId())
                    .thenCompose(hasBypass -> {
                        if (Boolean.TRUE.equals(hasBypass)) {
                            return failWithMessage("du kannst die rolle dieses spielers nicht anpassen");
                        }

                        return CompletableFuture.completedFuture(target);
                    });
            });
    }

    private CompletionStage<Void> updateRole(ProxiedPlayer player, ClanMember target, String roleName) {
        return loadRole(target.getClan(), roleName)
            .thenCompose(role -> clanMemberService.updateRole(target, role.getId()))
            .thenRun(() -> player.sendMessage("rolle gesetzt"));
    }

    private CompletionStage<ClanRole> loadRole(long clanId, String roleName) {
        return clanRoleService.getRoleByClanIdAndName(clanId, roleName)
            .thenCompose(role -> {
                if (role == null) {
                    return failWithMessage("rolle gibts nicht");
                }

                return CompletableFuture.completedFuture(role);
            });
    }
}
