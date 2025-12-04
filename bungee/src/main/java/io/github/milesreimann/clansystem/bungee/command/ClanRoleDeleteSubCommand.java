package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.bungee.plugin.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 04.12.25
 */
public class ClanRoleDeleteSubCommand extends ClanRoleCommand {
    private final ClanRoleService clanRoleService;

    public ClanRoleDeleteSubCommand(ClanSystemPlugin plugin) {
        super(ClanPermissionType.DELETE_ROLE);
        clanRoleService = plugin.getClanRoleService();
    }

    @Override
    public CompletionStage<Void> execute(ProxiedPlayer player, ClanMember clanMember, String[] args) {
        if (args.length == 0) {
            // help
            return CompletableFuture.completedStage(null);
        }

        String name = args[0];
        long clanId = clanMember.getClan();

        return clanRoleService.getRoleByClanIdAndName(clanId, name)
            .thenCompose(clanRole -> {
                if (clanRole == null) {
                    player.sendMessage("rolle gibts nicht");
                    return CompletableFuture.completedStage(null);
                }

                return clanRoleService.deleteRole(clanRole)
                    .thenRun(() -> player.sendMessage("rolle gelöscht"));
            });
    }
}