package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.bungee.plugin.ClanSystemPlugin;
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
        long clanId = clanMember.getClan();

        if (targetUuid.equals(clanMember.getUuid())) {
            player.sendMessage("du kannst deine eigene rolle nicht setzen");
            return CompletableFuture.completedStage(null);
        }

        return clanMemberService.getMemberByUuid(targetUuid)
            .thenCompose(targetClanMember -> {
                if (targetClanMember == null || targetClanMember.getClan() != clanId) {
                    player.sendMessage("spieler ist nicht in deinem clan");
                    return CompletableFuture.completedStage(null);
                }

                return clanRoleService.isRoleHigher(targetClanMember.getRole(), clanMember.getRole())
                    .thenCompose(isRoleHigher -> {
                        if (Boolean.TRUE.equals(isRoleHigher)) {
                            player.sendMessage("du kannst die rolle dieses spielers nicht anpassen");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanPermissionService.getPermissionByType(ClanPermissionType.SET_ROLE_BYPASS)
                            .thenCompose(setRoleBypassPermission -> {
                                if (setRoleBypassPermission == null) {
                                    return proceedRoleUpdate(player, targetClanMember, roleName);
                                }

                                return clanRolePermissionService.hasPermission(targetClanMember.getRole(), setRoleBypassPermission.getId())
                                    .thenCompose(hasSetRoleBypassPermission -> {
                                        if (Boolean.TRUE.equals(hasSetRoleBypassPermission)) {
                                            player.sendMessage("du kannst die rolle dieses spielers nicht anpassen");
                                            return CompletableFuture.completedStage(null);
                                        }

                                        return proceedRoleUpdate(player, targetClanMember, roleName);
                                    });
                            });
                    });
            });
    }

    private CompletionStage<Void> proceedRoleUpdate(ProxiedPlayer player, ClanMember targetClanMember, String roleName) {
        return clanRoleService.getRoleByClanIdAndName(targetClanMember.getClan(), roleName)
            .thenCompose(clanRole -> {
                if (clanRole == null) {
                    player.sendMessage("rolle gibts nicht");
                    return CompletableFuture.completedStage(null);
                }

                return clanMemberService.updateRole(targetClanMember, clanRole.getId())
                    .thenRun(() -> player.sendMessage("rolle gesetzt"));
            });
    }
}
