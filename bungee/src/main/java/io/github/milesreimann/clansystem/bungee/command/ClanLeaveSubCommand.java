package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public class ClanLeaveSubCommand extends ClanSubCommand {
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

        processLeaveRequest(player, playerUuid)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processLeaveRequest(ProxiedPlayer player, UUID playerUuid) {
        return loadMember(playerUuid)
            .thenCompose(member -> validateNotOwner(playerUuid, member))
            .thenCompose(member -> performLeave(player, member));
    }

    private CompletionStage<ClanMember> loadMember(UUID playerUuid) {
        return clanMemberService.getMemberByUuid(playerUuid)
            .thenCompose(member -> {
                if (member == null) {
                    return failWithMessage("du bist nicht in einem clan");
                }

                return CompletableFuture.completedFuture(member);
            });
    }

    private CompletionStage<ClanMember> validateNotOwner(UUID playerUuid, ClanMember member) {
        return clanService.getClanById(member.getClan())
            .thenCompose(clan -> {
                if (clan == null) {
                    return failWithMessage("clan gibts nicht");
                }

                if (playerUuid.equals(clan.getOwner())) {
                    return failWithMessage("du bist der clan owner und kannst nicht verlassen");
                }

                return CompletableFuture.completedFuture(member);
            });
    }

    private CompletionStage<Void> performLeave(ProxiedPlayer player, ClanMember member) {
        return clanMemberService.leaveClan(member)
            .thenRun(() -> player.sendMessage("verlassen"));
    }
}
