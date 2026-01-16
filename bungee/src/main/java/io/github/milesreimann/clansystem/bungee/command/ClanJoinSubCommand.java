package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
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
public class ClanJoinSubCommand extends ClanSubCommand {
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

        String clanName = args[0];

        processJoinRequest(player, player.getUniqueId(), clanName)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processJoinRequest(ProxiedPlayer player, UUID playerUuid, String clanName) {
        return validateNotInClan(playerUuid)
            .thenCompose(_ -> loadClan(clanName))
            .thenCompose(clan -> validateInvitationExists(clan, playerUuid))
            .thenCompose(clan -> acceptInvitation(player, clan, playerUuid));
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

    private CompletionStage<Clan> validateInvitationExists(Clan clan, UUID playerUuid) {
        return clanInvitationService.getInvitationByClanIdForPlayer(clan.getId(), playerUuid)
            .thenCompose(invitation -> {
                if (invitation == null) {
                    return failWithMessage("keine einladung erhalten");
                }

                return CompletableFuture.completedFuture(clan);
            });
    }

    private CompletionStage<Void> acceptInvitation(ProxiedPlayer player, Clan clan, UUID playerUuid) {
        return clanInvitationService.acceptInvitation(clan.getId(), playerUuid)
            .thenRun(() -> player.sendMessage("einladung angenommen"));
    }
}