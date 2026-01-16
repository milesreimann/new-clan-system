package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 04.12.25
 */
public class ClanRoleCreateSubCommand extends ClanRoleCommand {
    private final ClanRoleService clanRoleService;

    public ClanRoleCreateSubCommand(ClanSystemPlugin plugin) {
        super(plugin, ClanPermissionType.CREATE_ROLE);
        clanRoleService = plugin.getClanRoleService();
    }

    @Override
    public CompletionStage<Void> execute(ProxiedPlayer player, ClanMember clanMember, String[] args) {
        if (args.length == 0) {
            plugin.sendMessage(player, "clan-help-page-1");
            return CompletableFuture.completedStage(null);
        }

        String name = args[0];
        long clanId = clanMember.getClan();

        return processCreate(player, clanId, name)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processCreate(ProxiedPlayer player, long clanId, String name) {
        return validateRoleNotExists(clanId, name)
            .thenCompose(_ -> createRole(player, clanId, name));
    }

    private CompletionStage<Boolean> validateRoleNotExists(long clanId, String name) {
        return clanRoleService.getRoleByClanIdAndName(clanId, name)
            .thenCompose(role -> {
                if (role != null) {
                    return failWithMessage("clan-role-not-found");
                }

                return CompletableFuture.completedFuture(true);
            });
    }

    private CompletionStage<Void> createRole(ProxiedPlayer player, long clanId, String name) {
        return clanRoleService.createRole(clanId, name, null, null)
            .thenRun(() -> plugin.sendMessage(player, "clan-role-created", name));
    }
}