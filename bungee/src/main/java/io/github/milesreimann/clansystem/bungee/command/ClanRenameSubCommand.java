package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanService;
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
    private final ClanService clanService;

    public ClanRenameSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        clanService = plugin.getClanService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            plugin.sendMessage(player, "clan-help-page-1");
            return;
        }

        String newClanName = args[0];

        if (!plugin.getClanNameValidator().validate(player, newClanName)) {
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

                plugin.sendMessage(player, "no-clan");

                return clanMemberService.leaveClan(executor)
                    .thenCompose(_ -> CompletableFuture.completedFuture(null));
            });
    }

    private CompletionStage<Clan> validateNameChangeAllowed(Clan clan, String newClanName) {
        if (clan == null) {
            return failWithMessage("clan-not-found");
        }

        if (clan.getName().equals(newClanName)) {
            return failWithMessage("clan-rename-no-change");
        }

        return CompletableFuture.completedFuture(clan);
    }

    private CompletionStage<Clan> ensureNameAvailable(Clan clan, String newClanName) {
        if (clan == null) {
            return failWithMessage("clan-not-found");
        }

        if (clan.getName().equalsIgnoreCase(newClanName)) {
            return clanService.renameClan(clan.getId(), newClanName)
                .thenApply(_ -> clan);
        }

        return clanService.getClanByName(newClanName)
            .thenCompose(existingClan -> {
                if (existingClan != null) {
                    return failWithMessage("clan-already-exists");
                }

                return CompletableFuture.completedFuture(clan);
            });
    }

    private CompletionStage<Void> renameClan(ProxiedPlayer player, Clan clan, String newClanName) {
        if (clan == null) {
            return failWithMessage("clan-not-found");
        }

        return clanService.renameClan(clan.getId(), newClanName)
            .thenRun(() -> plugin.sendMessage(player, "clan-renamed", newClanName));
    }
}
