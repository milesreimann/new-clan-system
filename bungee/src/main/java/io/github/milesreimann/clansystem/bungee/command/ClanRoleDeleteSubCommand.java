package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.entity.ClanRole;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 04.12.25
 */
public class ClanRoleDeleteSubCommand extends ClanRoleCommand {
    private final ClanRoleService clanRoleService;
    private final ClanService clanService;

    public ClanRoleDeleteSubCommand(ClanSystemPlugin plugin) {
        super(ClanPermissionType.DELETE_ROLE);
        clanRoleService = plugin.getClanRoleService();
        clanService = plugin.getClanService();
    }

    @Override
    public CompletionStage<Void> execute(ProxiedPlayer player, ClanMember clanMember, String[] args) {
        if (args.length == 0) {
            // help
            return CompletableFuture.completedStage(null);
        }

        String name = args[0];
        long clanId = clanMember.getClan();

        return processDelete(player, clanId, name)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processDelete(ProxiedPlayer player, long clanId, String name) {
        return loadClan(clanId)
            .thenCompose(clan -> loadRole(clanId, name)
                .thenCompose(role -> validateRoleDeletable(clan, role))
                .thenCompose(role -> deleteRole(player, role)));
    }

    private CompletionStage<Clan> loadClan(long clanId) {
        return clanService.getClanById(clanId)
            .thenCompose(clan -> {
                if (clan == null) {
                    return failWithMessage("clan gibts nicht");
                }

                return CompletableFuture.completedFuture(clan);
            });
    }

    private CompletionStage<ClanRole> loadRole(long clanId, String name) {
        return clanRoleService.getRoleByClanIdAndName(clanId, name)
            .thenCompose(role -> {
                if (role == null) {
                    return failWithMessage("rolle gibts nicht");
                }

                return CompletableFuture.completedFuture(role);
            });
    }

    private CompletionStage<ClanRole> validateRoleDeletable(Clan clan, ClanRole role) {
        if (clan.getOwnerRole().equals(role.getId())) {
            return failWithMessage("owner rolle kann nicht gelöscht werden");
        }

        if (clan.getDefaultRole().equals(role.getId())) {
            return failWithMessage("die standardrolle kann nicht gelöscht werden. setze eine andere rolle als standard");
        }

        return CompletableFuture.completedFuture(role);
    }

    private CompletionStage<Void> deleteRole(ProxiedPlayer player, ClanRole role) {
        return clanRoleService.deleteRole(role)
            .thenRun(() -> player.sendMessage("rolle gelöscht"));
    }
}