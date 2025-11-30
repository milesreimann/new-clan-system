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
public class ClanRenameSubCommand implements ClanSubCommand {
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
        String newClanName = args[0];

        if (!config.isValidClanName(player, newClanName)) {
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

                        if (clan.getName().equals(newClanName)) {
                            player.sendMessage("name ist gleich");
                            return CompletableFuture.completedStage(null);
                        }

                        if (clan.getName().equalsIgnoreCase(newClanName)) {
                            return clanService.renameClan(clan.getId(), newClanName);
                        }

                        return clanService.getClanByName(newClanName)
                            .thenCompose(existingClan -> {
                                if (existingClan != null) {
                                    player.sendMessage("name existiert bereits");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanService.renameClan(clan.getId(), newClanName);
                            });
                    });
            });
    }
}
