package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanRequestSubCommand extends ClanSubCommand {
    private final ClanJoinRequestService clanJoinRequestService;
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    public ClanRequestSubCommand(ClanSystemPlugin plugin) {
        clanJoinRequestService = plugin.getClanJoinRequestService();
        clanService = plugin.getClanService();
        clanMemberService = plugin.getClanMemberService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        String clanName = args[0];

        processRequest(player, player.getUniqueId(), clanName)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processRequest(ProxiedPlayer player, UUID playerUuid, String clanName) {
        return validateNotInClan(playerUuid)
            .thenCompose(_ -> loadClan(clanName))
            .thenCompose(clan -> sendJoinRequest(player, playerUuid, clan));
    }

    private CompletionStage<Boolean> validateNotInClan(UUID playerUuid) {
        return clanMemberService.isInClan(playerUuid)
            .thenCompose(isInClan -> {
                if (Boolean.TRUE.equals(isInClan)) {
                    return failWithMessage("bereits in einem clan");
                }

                return CompletableFuture.completedFuture(true);
            });
    }

    private CompletionStage<Clan> loadClan(String clanName) {
        return clanService.getClanByName(clanName)
            .thenCompose(clan -> {
                if (clan == null) {
                    return failWithMessage("clan gibts nicht");
                }

                return CompletableFuture.completedFuture(clan);
            });
    }

    private CompletionStage<Void> sendJoinRequest(ProxiedPlayer player, UUID playerUuid, Clan clan) {
        return clanJoinRequestService.sendJoinRequest(playerUuid, clan.getId())
            .thenRun(() -> player.sendMessage("bereitsanfrage verschickt"));
    }
}
