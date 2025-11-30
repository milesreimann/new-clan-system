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
public class ClanRetagSubCommand implements ClanSubCommand {
    private final MainConfig config;
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        UUID playerUuid = player.getUniqueId();
        String newClanTag = args[0];

        if (!config.isValidClanTag(player, newClanTag)) {
            return;
        }

        // TODO: Permission Check

        clanMemberService.getMemberByUuid(playerUuid)
            .thenCompose(clanMember -> {
                if (clanMember == null) {
                    player.sendMessage("du bist nicht in einem clan");
                    return CompletableFuture.completedStage(null);
                }

                return clanService.getClanById(clanMember.getClan())
                    .thenCompose(clan -> {
                        if (clan == null) {
                            player.sendMessage("du bist nicht in einem clan");
                            return clanMemberService.leaveClan(clanMember);
                        }

                        if (clan.getTag().equals(newClanTag)) {
                            player.sendMessage("tag ist gleich");
                            return CompletableFuture.completedStage(null);
                        }

                        if (clan.getTag().equalsIgnoreCase(newClanTag)) {
                            return clanService.retagClan(clan.getId(), newClanTag);
                        }

                        return clanService.getClanByTag(newClanTag)
                            .thenCompose(existingClan -> {
                                if (existingClan != null) {
                                    player.sendMessage("tag existiert bereits");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanService.retagClan(clan.getId(), newClanTag);
                            });
                    });
            });
    }
}
