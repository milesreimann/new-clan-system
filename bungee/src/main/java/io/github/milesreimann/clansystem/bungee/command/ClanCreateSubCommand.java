package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.config.MainConfig;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
@RequiredArgsConstructor
public class ClanCreateSubCommand implements ClanSubCommand {
    private final MainConfig config;
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length < 2) {
            // help
            return;
        }

        UUID playerUuid = player.getUniqueId();
        String clanName = args[0];
        String clanTag = args[1];

        if (!config.isValidClanName(player, clanName)) {
            return;
        }

        if (!config.isValidClanTag(player, clanTag)) {
            return;
        }

        clanMemberService.isIsInClan(playerUuid)
            .thenCompose(isInClan -> {
                if (Boolean.TRUE.equals(isInClan)) {
                    player.sendMessage("du bist bereits in einem clan");
                    return CompletableFuture.completedStage(null);
                }

                return clanService.existsClanWithName(clanName)
                    .thenCompose(existsClanWithName -> {
                        if (existsClanWithName) {
                            player.sendMessage("name existiert bereits");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanService.existsClanWithTag(clanTag)
                            .thenCompose(existsClanWithTag -> {
                                if (existsClanWithTag) {
                                    player.sendMessage("tag existiert bereits");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanService.createClan(playerUuid, clanName, clanTag)
                                    .thenRun(() -> player.sendMessage("Clan erstellt"))
                                    .exceptionally(e -> {
                                        player.sendMessage("Fehler beim Clan erstellen: " + e.getCause().getMessage());
                                        return null;
                                    });
                            });
                    });
            })
            .exceptionally(t -> {
                t.printStackTrace();
                player.sendMessage(t.getMessage());
                return null;
            });
    }
}
