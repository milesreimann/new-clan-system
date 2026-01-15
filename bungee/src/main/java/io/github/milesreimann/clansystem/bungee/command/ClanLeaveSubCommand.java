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
public class ClanLeaveSubCommand implements ClanSubCommand {
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;

    public ClanLeaveSubCommand(ClanSystemPlugin plugin) {
        clanService = plugin.getClanService();
        clanMemberService = plugin.getClanMemberService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            player.sendMessage("/clan leave confirm");
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

                        if (clan.getOwner().equals(playerUuid)) {
                            player.sendMessage("du bist der clan owner und kannst nicht verlassen");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanMemberService.leaveClan(clanMember)
                            .thenRun(() -> player.sendMessage("verlassen"));
                    });
            });
    }
}
