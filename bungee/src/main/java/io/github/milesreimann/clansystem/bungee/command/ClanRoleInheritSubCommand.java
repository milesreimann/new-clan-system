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
public class ClanRoleInheritSubCommand extends ClanRoleCommand {
    private final ClanRoleService clanRoleService;

    public ClanRoleInheritSubCommand(ClanSystemPlugin plugin) {
        super(ClanPermissionType.INHERIT_ROLE);
        clanRoleService = plugin.getClanRoleService();
    }

    @Override
    public CompletionStage<Void> execute(ProxiedPlayer player, ClanMember clanMember, String[] args) {
        if (args.length < 2) {
            // help
            return CompletableFuture.completedStage(null);
        }

        String name = args[0];
        String inheritFrom = args[1];
        long clanId = clanMember.getClan();

        return clanRoleService.getRoleByClanIdAndName(clanId, name)
            .thenCompose(clanRole -> {
                if (clanRole == null) {
                    player.sendMessage("rolle gibts nicht");
                    return CompletableFuture.completedStage(null);
                }

                return clanRoleService.getRoleByClanIdAndName(clanId, inheritFrom)
                    .thenCompose(inheritFromClanRole -> {
                        if (inheritFromClanRole == null) {
                            player.sendMessage("die zu erbende rolle gibts nicht");
                            return CompletableFuture.completedStage(null);
                        }

                        return clanRoleService.isRoleHigher(inheritFromClanRole.getId(), clanMember.getRole())
                            .thenCompose(isRoleHigher -> {
                                if (Boolean.TRUE.equals(isRoleHigher)) {
                                    player.sendMessage("die zu erbende rolle ist höher als deine");
                                    return CompletableFuture.completedStage(null);
                                }

                                return clanRoleService.inheritRole(clanRole.getId(), inheritFromClanRole.getId())
                                    .thenRun(() -> player.sendMessage("rolle erbt nun"));
                            });
                    });
            });
    }
}