package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanMemberService;
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
public class ClanCreateSubCommand extends ClanSubCommand {
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    public ClanCreateSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        clanService = plugin.getClanService();
        clanMemberService = plugin.getClanMemberService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(player, "clan-help-page-1");
            return;
        }

        String clanName = args[0];
        String clanTag = args[1];

        if (!plugin.getClanNameValidator().validate(player, clanName)) {
            return;
        }

        if (!plugin.getClanTagValidator().validate(player, clanName)) {
            return;
        }

        processCreateRequest(player, clanName, clanTag)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processCreateRequest(ProxiedPlayer player, String clanName, String clanTag) {
        UUID playerUuid = player.getUniqueId();

        return validatePlayerNotInClan(playerUuid)
            .thenCompose(_ -> validateClanNameAvailable(clanName))
            .thenCompose(_ -> validateClanTagAvailable(clanTag))
            .thenCompose(_ -> createClan(player, playerUuid, clanName, clanTag));
    }

    private CompletionStage<Void> validatePlayerNotInClan(UUID playerUuid) {
        return clanMemberService.isInClan(playerUuid)
            .thenCompose(isInClan -> {
                if (Boolean.TRUE.equals(isInClan)) {
                    return failWithMessage("already-in-clan");
                }

                return CompletableFuture.completedStage(null);
            });
    }

    private CompletionStage<Void> validateClanNameAvailable(String clanName) {
        return clanService.existsClanWithName(clanName)
            .thenCompose(this::validateClanDoesNotExists);
    }

    private CompletionStage<Void> validateClanTagAvailable(String clanTag) {
        return clanService.existsClanWithTag(clanTag)
            .thenCompose(this::validateClanDoesNotExists);
    }

    private CompletionStage<Void> createClan(ProxiedPlayer player, UUID playerUuid, String clanName, String clanTag) {
        return clanService.createClan(playerUuid, clanName, clanTag)
            .thenRun(() -> plugin.sendMessage(player, "clan-created", clanName, clanTag));
    }

    private CompletionStage<Void> validateClanDoesNotExists(Boolean doesExist) {
        if (Boolean.TRUE.equals(doesExist)) {
            return failWithMessage("clan-already-exists");
        }

        return CompletableFuture.completedStage(null);
    }
}
