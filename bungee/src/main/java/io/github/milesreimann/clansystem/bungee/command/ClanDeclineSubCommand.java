package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.api.entity.ClanInvitation;
import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
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
public class ClanDeclineSubCommand extends ClanSubCommand {
    private final ClanInvitationService clanInvitationService;
    private final ClanService clanService;

    public ClanDeclineSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        clanInvitationService = plugin.getClanInvitationService();
        clanService = plugin.getClanService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        String clanName = args[0];

        processDeclineRequest(player, clanName)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processDeclineRequest(ProxiedPlayer player, String clanName) {
        return loadClan(clanName)
            .thenCompose(clan -> validateAndDeclineInvitation(player, clan));
    }

    private CompletionStage<Clan> loadClan(String clanName) {
        return clanService.getClanByName(clanName)
            .thenCompose(clan -> {
                if (clan == null) {
                    return failWithMessage("clan existiert nicht");
                }

                return CompletableFuture.completedStage(clan);
            });
    }

    private CompletionStage<Void> validateAndDeclineInvitation(ProxiedPlayer player, Clan clan) {
        UUID playerUuid = player.getUniqueId();

        return clanInvitationService.getInvitationByClanIdForPlayer(clan.getId(), playerUuid)
            .thenCompose(this::validateInvitationExists)
            .thenCompose(_ -> declineInvitation(player, clan.getId(), playerUuid));
    }

    private CompletionStage<ClanInvitation> validateInvitationExists(ClanInvitation invitation) {
        if (invitation == null) {
            return failWithMessage("du hast keine einladung vom clan erhalten");
        }

        return CompletableFuture.completedStage(invitation);
    }

    private CompletionStage<Void> declineInvitation(ProxiedPlayer player, Long clanId, UUID playerUuid) {
        return clanInvitationService.declineInvitation(clanId, playerUuid)
            .thenRun(() -> player.sendMessage("einladung abgelehnt"));
    }
}
