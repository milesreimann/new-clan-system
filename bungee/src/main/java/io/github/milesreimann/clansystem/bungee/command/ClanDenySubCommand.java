package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanDenySubCommand implements ClanSubCommand {
    private final ClanJoinRequestService clanJoinRequestService;
    private final ClanMemberService clanMemberService;
    private final ClanPermissionService clanPermissionService;
    private final ClanRolePermissionService clanRolePermissionService;

    public ClanDenySubCommand(ClanSystemPlugin plugin) {
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

                return clanPermissionService.getPermissionByType(ClanPermissionType.DENY_JOIN_REQUEST)
                    .thenCompose(denyPermission -> {
                        if (denyPermission == null) {
                            player.sendMessage("keine rechte");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanRolePermissionService.hasPermission(clanMember.getRole(), denyPermission.getId())
                            .thenCompose(hasDenyPermission -> {
                                if (!Boolean.TRUE.equals(hasDenyPermission)) {
                                    player.sendMessage("keine rechte");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanJoinRequestService.denyJoinRequest(playerUuid, clanMember.getClan())
                                    .thenRun(() -> player.sendMessage("anfrage abgelehnt"));
                            });
                    });
            });
    }
}
