package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanJoinSubCommand implements ClanSubCommand {
    private final ClanInvitationService clanInvitationService;
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    public ClanJoinSubCommand(ClanSystemPlugin plugin) {
        clanInvitationService = plugin.getClanInvitationService();
        clanService = plugin.getClanService();
        clanMemberService = plugin.getClanMemberService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        UUID playerUuid = player.getUniqueId();
        String clanName = args[0];

        clanMemberService.isInClan(playerUuid)
            .thenCompose(isInClan -> {
                if (Boolean.TRUE.equals(isInClan)) {
                    player.sendMessage("bereits in einem clan");
                    return CompletableFuture.completedStage(null);
                }

                return clanService.getClanByName(clanName)
                    .thenCompose(clan -> {
                        if (clan == null) {
                            player.sendMessage("clan gibts nicht");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanInvitationService.getInvitationByClanIdForPlayer(clan.getId(), playerUuid)
                            .thenCompose(clanInvitation -> {
                                if (clanInvitation == null) {
                                    player.sendMessage("keine einladung erhalten");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanInvitationService.acceptInvitation(clan.getId(), playerUuid)
                                    .thenRun(() -> player.sendMessage("einladung angenommen"));
                            });
                    });
            });
    }
}
