package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.entity.ClanRole;
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

        return processInherit(player, clanMember, clanId, name, inheritFrom)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processInherit(
        ProxiedPlayer player,
        ClanMember executor,
        long clanId,
        String name,
        String inheritFrom
    ) {
        return loadRole(clanId, name, "rolle gibts nicht")
            .thenCompose(role -> loadRole(clanId, inheritFrom, "die zu erbende rolle gibts nicht")
                .thenCompose(inheritFromRole -> validateInheritRoleAllowed(executor, inheritFromRole)
                    .thenCompose(_ -> inheritRole(player, role, inheritFromRole.getId()))));
    }

    private CompletionStage<ClanRole> loadRole(long clanId, String name, String errorMessage) {
        return clanRoleService.getRoleByClanIdAndName(clanId, name)
            .thenCompose(role -> {
                if (role == null) {
                    return failWithMessage(errorMessage);
                }

                return CompletableFuture.completedFuture(role);
            });
    }

    private CompletionStage<Boolean> validateInheritRoleAllowed(ClanMember executor, ClanRole inheritFromRole) {
        return clanRoleService.isRoleHigher(inheritFromRole.getId(), executor.getRole())
            .thenCompose(isRoleHigher -> {
                if (Boolean.TRUE.equals(isRoleHigher)) {
                    return failWithMessage("die zu erbende rolle ist höher als deine");
                }

                return CompletableFuture.completedFuture(true);
            });
    }

    private CompletionStage<Void> inheritRole(ProxiedPlayer player, ClanRole role, Long inheritFromRoleId) {
        return clanRoleService.inheritRole(role, inheritFromRoleId)
            .thenRun(() -> player.sendMessage("rolle erbt nun"));
    }
}