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
public class ClanKickSubCommand implements ClanSubCommand {
    private final ClanMemberService clanMemberService;
    private final ClanPermissionService clanPermissionService;
    private final ClanRolePermissionService clanRolePermissionService;
    private final ClanRoleService clanRoleService;

    public ClanKickSubCommand(ClanSystemPlugin plugin) {
        clanMemberService = plugin.getClanMemberService();
        clanPermissionService = plugin.getClanPermissionService();
        clanRolePermissionService = plugin.getClanRolePermissionService();
        clanRoleService = plugin.getClanRoleService();
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

                return clanPermissionService.getPermissionByType(ClanPermissionType.KICK_MEMBER)
                    .thenCompose(kickPermission -> {
                        if (kickPermission == null) {
                            player.sendMessage("keine rechte");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanRolePermissionService.hasPermission(clanMember.getRole(), kickPermission.getId())
                            .thenCompose(hasKickPermission -> {
                                if (!Boolean.TRUE.equals(hasKickPermission)) {
                                    player.sendMessage("keine rechte");
                                    return CompletableFuture.completedStage(null);
                                }

                                if (targetUuid.equals(playerUuid)) {
                                    player.sendMessage("du kannst dich nicht selbst kicken");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanMemberService.getMemberByUuid(targetUuid)
                                    .thenCompose(targetClanMember -> {
                                        if (targetClanMember == null || !targetClanMember.getClan().equals(clanMember.getClan())) {
                                            player.sendMessage("der spieler ist nicht im selben clan wie du");
                                            return CompletableFuture.completedStage(null);
                                        }

                                        return clanRoleService.isRoleHigher(targetClanMember.getRole(), clanMember.getRole())
                                            .thenCompose(isRoleHigher -> {
                                                if (Boolean.TRUE.equals(isRoleHigher)) {
                                                    player.sendMessage("du kannst den spieler nicht kicken");
                                                    return CompletableFuture.completedStage(null);
                                                }

                                                return clanPermissionService.getPermissionByType(ClanPermissionType.KICK_MEMBER_BYPASS)
                                                    .thenCompose(bypassKickPermission -> {
                                                        if (bypassKickPermission == null) {
                                                            return executeKick(player, targetClanMember);
                                                        }

                                                        return clanRolePermissionService.hasPermission(targetClanMember.getRole(), bypassKickPermission.getId())
                                                            .thenCompose(hasBypassKickPermission -> {
                                                                if (Boolean.TRUE.equals(hasBypassKickPermission)) {
                                                                    player.sendMessage("du kannst den spieler nicht kicken");
                                                                    return CompletableFuture.completedStage(null);
                                                                }

                                                                return executeKick(player, targetClanMember);
                                                            });
                                                    });
                                            });
                                    });
                            });
                    });
            });
    }

    private CompletionStage<Void> executeKick(ProxiedPlayer player, ClanMember clanMember) {
        return clanMemberService.leaveClan(clanMember)
            .thenRun(() -> player.sendMessage("spieler wurde gekickt"));
    }
}