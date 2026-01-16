package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.config.MainConfig;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public class ClanRetagSubCommand extends AuthorizedClanSubCommand {
    private final MainConfig config;
    private final ClanService clanService;

    public ClanRetagSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        config = plugin.getConfig();
        clanService = plugin.getClanService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        String newClanTag = args[0];

        if (!config.isValidClanTag(player, newClanTag)) {
            return;
        }

        processRetagRequest(player, player.getUniqueId(), newClanTag)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processRetagRequest(ProxiedPlayer player, UUID playerUuid, String newClanTag) {
        return loadExecutorWithPermissions(playerUuid, ClanPermissionType.RETAG_CLAN)
            .thenCompose(executor -> loadClanOrCleanup(player, executor))
            .thenCompose(clan -> validateTagChangeAllowed(clan, newClanTag))
            .thenCompose(clan -> ensureTagAvailable(clan, newClanTag))
            .thenCompose(clan -> retagClan(player, clan, newClanTag));
    }


    private CompletionStage<Clan> loadClanOrCleanup(ProxiedPlayer player, ClanMember executor) {
        return clanService.getClanById(executor.getClan())
            .thenCompose(clan -> {
                if (clan != null) {
                    return CompletableFuture.completedFuture(clan);
                }

                player.sendMessage("du bist nicht in einem clan");

                return clanMemberService.leaveClan(executor)
                    .thenCompose(_ -> CompletableFuture.completedFuture(null));
            });
    }

    private CompletionStage<Clan> validateTagChangeAllowed(Clan clan, String newClanTag) {
        if (clan == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (clan.getTag().equals(newClanTag)) {
            return failWithMessage("tag ist gleich");
        }

        return CompletableFuture.completedFuture(clan);
    }

    private CompletionStage<Clan> ensureTagAvailable(Clan clan, String newClanTag) {
        if (clan == null) {
            return failWithMessage("clan gibts nicht");
        }

        if (clan.getTag().equalsIgnoreCase(newClanTag)) {
            return clanService.retagClan(clan.getId(), newClanTag)
                .thenApply(_ -> clan);
        }

        return clanService.getClanByTag(newClanTag)
            .thenCompose(existingClan -> {
                if (existingClan != null) {
                    return failWithMessage("tag existiert bereits");
                }

                return CompletableFuture.completedFuture(clan);
            });
    }

    private CompletionStage<Void> retagClan(ProxiedPlayer player, Clan clan, String newClanTag) {
        if (clan == null) {
            return failWithMessage("clan gibts nicht");
        }

        return clanService.retagClan(clan.getId(), newClanTag)
            .thenRun(() -> player.sendMessage("tag geändert"));
    }
}
