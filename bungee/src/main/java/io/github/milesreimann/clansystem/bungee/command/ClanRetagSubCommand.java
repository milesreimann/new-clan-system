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
    private final ClanService clanService;

    public ClanRetagSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        clanService = plugin.getClanService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            plugin.sendMessage(player, "clan-help-page-1");
            return;
        }

        String newClanTag = args[0];

        if (!plugin.getClanTagValidator().validate(player, newClanTag)) {
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

                plugin.sendMessage(player, "no-clan");

                return clanMemberService.leaveClan(executor)
                    .thenCompose(_ -> CompletableFuture.completedFuture(null));
            });
    }

    private CompletionStage<Clan> validateTagChangeAllowed(Clan clan, String newClanTag) {
        if (clan == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (clan.getTag().equals(newClanTag)) {
            return failWithMessage("clan-retag-no-changes");
        }

        return CompletableFuture.completedFuture(clan);
    }

    private CompletionStage<Clan> ensureTagAvailable(Clan clan, String newClanTag) {
        if (clan == null) {
            return failWithMessage("clan-not-found");
        }

        if (clan.getTag().equalsIgnoreCase(newClanTag)) {
            return clanService.retagClan(clan.getId(), newClanTag)
                .thenApply(_ -> clan);
        }

        return clanService.getClanByTag(newClanTag)
            .thenCompose(existingClan -> {
                if (existingClan != null) {
                    return failWithMessage("clan-tag-already-exists");
                }

                return CompletableFuture.completedFuture(clan);
            });
    }

    private CompletionStage<Void> retagClan(ProxiedPlayer player, Clan clan, String newClanTag) {
        if (clan == null) {
            return failWithMessage("clan-not-found");
        }

        return clanService.retagClan(clan.getId(), newClanTag)
            .thenRun(() -> plugin.sendMessage(player, "clan-retagged", newClanTag));
    }
}
