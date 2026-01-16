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
        super(plugin, ClanPermissionType.INHERIT_ROLE);
        clanRoleService = plugin.getClanRoleService();
    }

    @Override
    public CompletionStage<Void> execute(ProxiedPlayer player, ClanMember clanMember, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(player, "clan-help-page-1");
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
        return loadRole(clanId, name, "clan-role-not-found")
            .thenCompose(role -> loadRole(clanId, inheritFrom, "clan-role-inherit-target-role-not-found")
                .thenCompose(inheritFromRole -> validateInheritRoleAllowed(executor, inheritFromRole)
                    .thenCompose(_ -> inheritRole(player, role, inheritFromRole))
                )
            );
    }

    private CompletionStage<ClanRole> loadRole(long clanId, String name, String errorKey) {
        return clanRoleService.getRoleByClanIdAndName(clanId, name)
            .thenCompose(role -> {
                if (role == null) {
                    return failWithMessage(errorKey);
                }

                return CompletableFuture.completedFuture(role);
            });
    }

    private CompletionStage<Boolean> validateInheritRoleAllowed(ClanMember executor, ClanRole inheritFromRole) {
        return clanRoleService.isRoleHigher(inheritFromRole.getId(), executor.getRole())
            .thenCompose(isRoleHigher -> {
                if (Boolean.TRUE.equals(isRoleHigher)) {
                    return failWithMessage("clan-role-inherit-target-role-higher-than-self");
                }

                return CompletableFuture.completedFuture(true);
            });
    }

    private CompletionStage<Void> inheritRole(ProxiedPlayer player, ClanRole role, ClanRole inheritFromRole) {
        return clanRoleService.inheritRole(role, inheritFromRole.getId())
            .thenRun(() -> plugin.sendMessage(player, "clan.role.inherit.success", role.getName(), inheritFromRole.getName()));
    }
}