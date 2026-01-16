package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.Clan;
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
public class ClanDeleteSubCommand extends ClanSubCommand {
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

        processDeleteRequest(player)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processDeleteRequest(ProxiedPlayer player) {
        UUID playerUuid = player.getUniqueId();

        return loadMemberAndClan(playerUuid)
            .thenCompose(member -> validateOwnershipAndDelete(player, member, playerUuid));
    }

    private CompletionStage<ClanMember> loadMemberAndClan(UUID playerUuid) {
        return clanMemberService.getMemberByUuid(playerUuid)
            .thenCompose(member -> {
                if (member == null) {
                    return failWithMessage("du bist nicht in einem clan");
                }

                return CompletableFuture.completedStage(member);
            });
    }

    private CompletionStage<Void> validateOwnershipAndDelete(
        ProxiedPlayer player,
        ClanMember member,
        UUID playerUuid
    ) {
        return clanService.getClanById(member.getClan())
            .thenCompose(clan -> {
                if (clan == null) {
                    return handleOrphanedMembership(member);
                }

                return validateOwnershipAndExecuteDelete(player, clan, playerUuid);
            });
    }

    private CompletionStage<Void> handleOrphanedMembership(ClanMember member) {
        return clanMemberService.leaveClan(member);
    }

    private CompletionStage<Void> validateOwnershipAndExecuteDelete(ProxiedPlayer player, Clan clan, UUID playerUuid) {
        if (!clan.getOwner().equals(playerUuid)) {
            return failWithMessage("nur der owner kann den clan löschen");
        }

        return deleteClan(player, clan);
    }

    private CompletionStage<Void> deleteClan(ProxiedPlayer player, Clan clan) {
        return clanService.deleteClan(clan)
            .thenRun(() -> player.sendMessage("clan wurde gelöscht"));
    }
}
