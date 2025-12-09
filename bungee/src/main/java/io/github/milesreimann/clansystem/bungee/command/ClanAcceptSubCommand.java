package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.bungee.plugin.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanAcceptSubCommand implements ClanSubCommand {
    private final ClanJoinRequestService clanJoinRequestService;
    private final ClanMemberService clanMemberService;
    private final ClanPermissionService clanPermissionService;
    private final ClanRolePermissionService clanRolePermissionService;

    public ClanAcceptSubCommand(ClanSystemPlugin plugin) {
        clanJoinRequestService = plugin.getClanJoinRequestService();
        clanMemberService = plugin.getClanMemberService();
        clanPermissionService = plugin.getClanPermissionService();
        clanRolePermissionService = plugin.getClanRolePermissionService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        UUID playerUuid = player.getUniqueId();

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("ungültige uuid");
            return;
        }

        clanMemberService.getMemberByUuid(playerUuid)
            .thenCompose(clanMember -> {
                if (clanMember == null) {
                    player.sendMessage("bist in keinem clan");
                    return CompletableFuture.completedStage(null);
                }

                return clanPermissionService.getPermissionByType(ClanPermissionType.ACCEPT_JOIN_REQUEST)
                    .thenCompose(acceptPermission -> {
                        if (acceptPermission == null) {
                            player.sendMessage("keine rechte");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanRolePermissionService.hasPermission(clanMember.getRole(), acceptPermission.getId())
                            .thenCompose(hasAcceptPermission -> {
                                if (!Boolean.TRUE.equals(hasAcceptPermission)) {
                                    player.sendMessage("keine rechte");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanMemberService.isInClan(targetUuid)
                                    .thenCompose(isTargetInClan -> {
                                        if (Boolean.TRUE.equals(isTargetInClan)) {
                                            player.sendMessage("er ist bereits in einem clan");
                                            return clanJoinRequestService.denyJoinRequest(targetUuid, clanMember.getClan());
                                        }

                                        return clanJoinRequestService.acceptJoinRequest(targetUuid, clanMember.getClan())
                                            .thenRun(() -> player.sendMessage("anfrage wurde angenommen"));
                                    });
                            });
                    });
            });
    }
}
