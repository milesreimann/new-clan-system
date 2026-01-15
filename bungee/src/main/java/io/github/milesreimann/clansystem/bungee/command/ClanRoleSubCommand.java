package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanPermission;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 04.12.25
 */
public class ClanRoleSubCommand implements ClanSubCommand {
    private final ClanMemberService clanMemberService;
    private final ClanPermissionService clanPermissionService;
    private final ClanRolePermissionService clanRolePermissionService;

    private final Map<String, ClanRoleCommand> clanRoleCommandMap = new HashMap<>();

    public ClanRoleSubCommand(ClanSystemPlugin plugin) {
        clanMemberService = plugin.getClanMemberService();
        clanPermissionService = plugin.getClanPermissionService();
        clanRolePermissionService = plugin.getClanRolePermissionService();

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

        UUID playerUuid = player.getUniqueId();
        String operation = args[0].toLowerCase();

        ClanRoleCommand clanRoleCommand = clanRoleCommandMap.get(operation);
        if (clanRoleCommand == null) {
            player.sendMessage("unbekannter befehl");
            return;
        }

        clanMemberService.getMemberByUuid(playerUuid)
            .thenCompose(clanMember -> {
                if (clanMember == null) {
                    player.sendMessage("du bist in keinem clan");
                    return CompletableFuture.completedStage(null);
                }

                return clanPermissionService.listPermissionsByTypes(clanRoleCommand.getClanPermissionType(), ClanPermissionType.MANAGE_ROLES)
                    .thenCompose(clanPermissions -> {
                        long[] clanPermissionIds = clanPermissions.stream()
                            .mapToLong(ClanPermission::getId)
                            .toArray();

                        return clanRolePermissionService.hasAnyPermission(clanMember.getRole(), clanPermissionIds)
                            .thenCompose(hasPermission -> {
                                if (!Boolean.TRUE.equals(hasPermission)) {
                                    player.sendMessage("das darfst du nicht");
                                    return CompletableFuture.completedStage(null);
                                }

                                String[] roleCommandArgs = buildRoleCommandArgs(args);
                                return clanRoleCommand.execute(player, clanMember, roleCommandArgs);
                            });
                    });
            })
            .exceptionally(t -> {
                t.printStackTrace();
                return null;
            });
    }

    private String[] buildRoleCommandArgs(String[] args) {
        return Arrays.stream(args)
            .skip(1)
            .toArray(String[]::new);
    }
}
