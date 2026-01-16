package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
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
public class ClanKickSubCommand extends AuthorizedClanSubCommand {
    private final ClanRoleService clanRoleService;
    private final ClanMemberService clanMemberService;

    public ClanKickSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        clanRoleService = plugin.getClanRoleService();
        clanMemberService = plugin.getClanMemberService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("ungültige uuid");
            return;
        }

        processKickRequest(player, targetUuid)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processKickRequest(ProxiedPlayer player, UUID targetUuid) {
        return loadExecutorWithPermissions(player.getUniqueId(), ClanPermissionType.KICK_MEMBER)
            .thenCompose(executor -> validateAndExecuteKick(player, executor, targetUuid));
    }

    private CompletionStage<Void> validateAndExecuteKick(
        ProxiedPlayer player,
        ClanMember executor,
        UUID targetUuid
    ) {
        if (targetUuid.equals(executor.getUuid())) {
            return failWithMessage("du kannst dich nicht selbst kicken");
        }

        return loadAndValidateTarget(executor, targetUuid)
            .thenCompose(target -> performKick(player, target));
    }

    private CompletionStage<ClanMember> loadAndValidateTarget(ClanMember executor, UUID targetUuid) {
        return clanMemberService.getMemberByUuid(targetUuid)
            .thenCompose(target -> validateTargetInSameClan(executor, target))
            .thenCompose(target -> validateRoleHierarchy(executor, target))
            .thenCompose(this::validateBypassProtection);
    }

    private CompletableFuture<Void> performKick(ProxiedPlayer player, ClanMember target) {
        return clanMemberService.leaveClan(target)
            .toCompletableFuture()
            .thenRun(() -> player.sendMessage("spieler wurde gekickt"));
    }

    private CompletableFuture<ClanMember> validateTargetInSameClan(ClanMember executor, ClanMember target) {
        if (target == null || !target.getClan().equals(executor.getClan())) {
            return failWithMessage("Der Spieler ist nicht im selben Clan.");
        }

        return CompletableFuture.completedFuture(target);
    }

    private CompletionStage<ClanMember> validateRoleHierarchy(ClanMember executor, ClanMember target) {
        return clanRoleService.isRoleHigher(target.getRole(), executor.getRole())
            .thenCompose(isTargetRoleHigher -> validateRoleIsLower(target, isTargetRoleHigher));
    }

    private CompletionStage<ClanMember> validateRoleIsLower(ClanMember target, Boolean isTargetRoleHigher) {
        if (!Boolean.TRUE.equals(isTargetRoleHigher)) {
            return failWithMessage("du kannst kein mitglied mit höherer rolle kicken");
        }

        return CompletableFuture.completedFuture(target);
    }

    private CompletionStage<ClanMember> validateBypassProtection(ClanMember target) {
        return clanPermissionService.getPermissionByType(ClanPermissionType.KICK_MEMBER_BYPASS)
            .thenCompose(bypassPermission -> {
                if (bypassPermission == null) {
                    return failWithMessage("bypass-berechtigung existiert nicht");
                }

                return clanRolePermissionService.hasPermission(target.getRole(), bypassPermission.getId());
            })
            .thenCompose(hasBypassPermission -> {
                if (Boolean.TRUE.equals(hasBypassPermission)) {
                    return failWithMessage("du kannst den spieler nicht kicken");
                }

                return CompletableFuture.completedFuture(target);
            });
    }
}