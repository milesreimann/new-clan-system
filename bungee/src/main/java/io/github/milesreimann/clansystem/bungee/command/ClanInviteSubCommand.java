package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
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
public class ClanInviteSubCommand implements ClanSubCommand {
    private final ClanInvitationService clanInvitationService;
    private final ClanMemberService clanMemberService;
    private final ClanPermissionService clanPermissionService;
    private final ClanRolePermissionService clanRolePermissionService;

    public ClanInviteSubCommand(ClanSystemPlugin plugin) {
        clanInvitationService = plugin.getClanInvitationService();
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
                    player.sendMessage("du bist nicht in einem clan");
                    return CompletableFuture.completedStage(null);
                }

                return clanPermissionService.getPermissionByType(ClanPermissionType.SEND_INVITATION)
                    .thenCompose(invitePermission -> {
                        if (invitePermission == null) {
                            player.sendMessage("keine rechte");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanRolePermissionService.hasPermission(clanMember.getRole(), invitePermission.getId())
                            .thenCompose(hasInvitePermission -> {
                                if (!Boolean.TRUE.equals(hasInvitePermission)) {
                                    player.sendMessage("keine rechte");
                                    return CompletableFuture.completedStage(null);
                                }

                                if (targetUuid.equals(playerUuid)) {
                                    player.sendMessage("du kannst dich nicht selbst einladen");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanMemberService.isInClan(targetUuid)
                                    .thenCompose(isTargetInClan -> {
                                        if (Boolean.TRUE.equals(isTargetInClan)) {
                                            player.sendMessage("spieler ist bereits in einem clan");
                                            return CompletableFuture.completedStage(null);
                                        }

                                        return clanInvitationService.sendInvitation(clanMember.getClan(), clanMember.getClan(), targetUuid)
                                            .thenRun(() -> player.sendMessage("einladung versendet"));
                                    });
                            });
                    });
            });
    }
}
