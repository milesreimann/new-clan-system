package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanMemberService;
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
public class ClanCreateSubCommand extends ClanSubCommand {
    private final MainConfig config;
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    public ClanCreateSubCommand(ClanSystemPlugin plugin) {
        config = plugin.getConfig();
        clanService = plugin.getClanService();
        clanMemberService = plugin.getClanMemberService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length < 2) {
            // help
            return;
        }

        String clanName = args[0];
        String clanTag = args[1];

        if (!config.isValidClanName(player, clanName)) {
            return;
        }

        if (!config.isValidClanTag(player, clanTag)) {
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
                    return failWithMessage("du bist bereits in einem clan");
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
            .thenRun(() -> player.sendMessage("clan wurde erstellt"));
    }

    private CompletionStage<Void> validateClanDoesNotExists(Boolean doesExist) {
        if (Boolean.TRUE.equals(doesExist)) {
            return failWithMessage("clan existiert bereits");
        }

        return CompletableFuture.completedStage(null);
    }
}
