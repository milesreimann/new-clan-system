package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.plugin.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanDeclineSubCommand implements ClanSubCommand {
    private final ClanInvitationService clanInvitationService;
    private final ClanService clanService;

    public ClanDeclineSubCommand(ClanSystemPlugin plugin) {
        clanInvitationService = plugin.getClanInvitationService();
        clanService = plugin.getClanService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        UUID playerUuid = player.getUniqueId();
        String clanName = args[0];

        clanService.getClanByName(clanName)
            .thenCompose(clan -> {
                if (clan == null) {
                    player.sendMessage("clan gibts nicht");
                    return CompletableFuture.completedStage(null);
                }

                return clanInvitationService.getInvitationByClanIdForPlayer(clan.getId(), playerUuid)
                    .thenCompose(clanInvitation -> {
                        if (clanInvitation == null) {
                            player.sendMessage("keine einladung");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanInvitationService.declineInvitation(clan.getId(), playerUuid)
                            .thenRun(() -> player.sendMessage("einladung abgelehnt"));
                    });
            });
    }
}
