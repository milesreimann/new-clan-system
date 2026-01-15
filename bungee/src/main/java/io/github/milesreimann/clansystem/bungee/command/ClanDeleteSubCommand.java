package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public class ClanDeleteSubCommand implements ClanSubCommand {
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    public ClanDeleteSubCommand(ClanSystemPlugin plugin) {
        clanService = plugin.getClanService();
        clanMemberService = plugin.getClanMemberService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            player.sendMessage("/clan delete confirm");
            return;
        }

        UUID playerUuid = player.getUniqueId();

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

                        if (!clan.getOwner().equals(playerUuid)) {
                            player.sendMessage("nur der owner darf das");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanService.deleteClan(clan)
                            .thenRun(() -> player.sendMessage("clan wurde gelöscht"));
                    });
            });
    }
}
