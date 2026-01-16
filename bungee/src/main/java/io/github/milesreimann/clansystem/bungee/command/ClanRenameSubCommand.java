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
public class ClanRenameSubCommand extends AuthorizedClanSubCommand {
    private final MainConfig config;
    private final ClanService clanService;

    public ClanRenameSubCommand(ClanSystemPlugin plugin) {
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

        String newClanName = args[0];

        if (!config.isValidClanName(player, newClanName)) {
            return;
        }

        processRenameRequest(player, player.getUniqueId(), newClanName)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processRenameRequest(ProxiedPlayer player, UUID playerUuid, String newClanName) {
        return loadExecutorWithPermissions(playerUuid, ClanPermissionType.RENAME_CLAN)
            .thenCompose(executor -> loadClanOrCleanup(player, executor))
            .thenCompose(clan -> validateNameChangeAllowed(clan, newClanName))
            .thenCompose(clan -> ensureNameAvailable(clan, newClanName))
            .thenCompose(clan -> renameClan(player, clan, newClanName));
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

    private CompletionStage<Clan> validateNameChangeAllowed(Clan clan, String newClanName) {
        if (clan == null) {
            return failWithMessage("clan gibts nicht");
        }

        if (clan.getName().equals(newClanName)) {
            return failWithMessage("name ist gleich");
        }

        return CompletableFuture.completedFuture(clan);
    }

    private CompletionStage<Clan> ensureNameAvailable(Clan clan, String newClanName) {
        if (clan == null) {
            return failWithMessage("clan gibts nicht");
        }

        if (clan.getName().equalsIgnoreCase(newClanName)) {
            return clanService.renameClan(clan.getId(), newClanName)
                .thenApply(_ -> clan);
        }

        return clanService.getClanByName(newClanName)
            .thenCompose(existingClan -> {
                if (existingClan != null) {
                    return failWithMessage("name existiert bereits");
                }

                return CompletableFuture.completedFuture(clan);
            });
    }

    private CompletionStage<Void> renameClan(ProxiedPlayer player, Clan clan, String newClanName) {
        if (clan == null) {
            return failWithMessage("clan gibts nicht");
        }

        return clanService.renameClan(clan.getId(), newClanName)
            .thenRun(() -> player.sendMessage("name"));
    }
}
