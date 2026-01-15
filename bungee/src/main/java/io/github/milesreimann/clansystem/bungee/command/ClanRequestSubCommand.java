package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
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
public class ClanRequestSubCommand implements ClanSubCommand {
    private final ClanJoinRequestService clanJoinRequestService;
    private final ClanMemberService clanMemberService;
    private final ClanService clanService;

    public ClanRequestSubCommand(ClanSystemPlugin plugin) {
        clanJoinRequestService = plugin.getClanJoinRequestService();
        clanMemberService = plugin.getClanMemberService();
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

                        // TODO: clan settings, toggle requests

                        return clanJoinRequestService.sendJoinRequest(playerUuid, clan.getId())
                            .thenRun(() -> player.sendMessage("bereitsanfrage verschickt"));
                    });
            });
    }
}
