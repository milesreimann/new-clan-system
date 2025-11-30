package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
@RequiredArgsConstructor
public class ClanLeaveSubCommand implements ClanSubCommand {
    private final ClanService clanService;
    private final ClanMemberService clanMemberService;
    private final ClanRoleService clanRoleService;

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

                        if (clanMember.getRole() == null) {
                            return clanMemberService.leaveClan(clanMember);
                        }

                        return clanRoleService.getRoleById(clanMember.getRole())
                            .thenCompose(clanRole -> {
                                if (clanRole == null) {
                                    return clanMemberService.leaveClan(clanMember);
                                }

                                if (clanRole.isOwnerRole()) {
                                    player.sendMessage("du bist der clan owner und kannst nicht verlassen");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanMemberService.leaveClan(clanMember);
                            });
                    });
            });
    }
}
