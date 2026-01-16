package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 04.12.25
 */
public class ClanRoleSubCommand extends AuthorizedClanSubCommand {
    private final Map<String, ClanRoleCommand> clanRoleCommandMap = new HashMap<>();

    public ClanRoleSubCommand(ClanSystemPlugin plugin) {
        super(plugin);

        clanRoleCommandMap.put("create", new ClanRoleCreateSubCommand(plugin));
        clanRoleCommandMap.put("delete", new ClanRoleDeleteSubCommand(plugin));
        clanRoleCommandMap.put("inherit", new ClanRoleInheritSubCommand(plugin));
        clanRoleCommandMap.put("set", new ClanRoleSetSubCommand(plugin));
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length < 1) {
            // help
            return;
        }

        String operation = args[0].toLowerCase();
        ClanRoleCommand clanRoleCommand = clanRoleCommandMap.get(operation);

        if (clanRoleCommand == null) {
            player.sendMessage("unbekannter befehl");
            // help
            return;
        }

        processRoleCommand(player, player.getUniqueId(), clanRoleCommand, buildRoleCommandArgs(args))
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processRoleCommand(
        ProxiedPlayer player,
        UUID playerUuid,
        ClanRoleCommand command,
        String[] roleArgs
    ) {
        return clanMemberService.getMemberByUuid(playerUuid)
            .thenCompose(this::validateExecutorInClan)
            .thenCompose(member -> validateHasAnyRolePermission(member, command))
            .thenCompose(member -> command.execute(player, member, roleArgs));
    }

    private CompletionStage<ClanMember> validateExecutorInClan(ClanMember clanMember) {
        if (clanMember == null) {
            return failWithMessage("du bist in keinem clan");
        }

        return CompletableFuture.completedFuture(clanMember);
    }

    private CompletionStage<ClanMember> validateHasAnyRolePermission(ClanMember member, ClanRoleCommand command) {
        return clanPermissionService.listPermissionsByTypes(command.getClanPermissionType(), ClanPermissionType.MANAGE_ROLES)
            .thenCompose(permissions -> {
                long[] ids = permissions.stream()
                    .mapToLong(ClanPermission::getId)
                    .toArray();

                return clanRolePermissionService.hasAnyPermission(member.getRole(), ids);
            })
            .thenCompose(hasPermission -> {
                if (!Boolean.TRUE.equals(hasPermission)) {
                    return failWithMessage("das darfst du nicht");
                }

                return CompletableFuture.completedFuture(member);
            });
    }

    private String[] buildRoleCommandArgs(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }

        return Arrays.copyOfRange(args, 1, args.length);
    }
}
