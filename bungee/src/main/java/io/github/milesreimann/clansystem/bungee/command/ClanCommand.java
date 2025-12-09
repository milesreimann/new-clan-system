package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.bungee.plugin.ClanSystemPlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public class ClanCommand extends Command {
    private final Map<String, ClanSubCommand> subCommands = new HashMap<>();

    public ClanCommand(ClanSystemPlugin plugin) {
        super("clan");
        subCommands.put("create", new ClanCreateSubCommand(plugin));
        subCommands.put("rename", new ClanRenameSubCommand(plugin));
        subCommands.put("retag", new ClanRetagSubCommand(plugin));
        subCommands.put("leave", new ClanLeaveSubCommand(plugin));
        subCommands.put("delete", new ClanDeleteSubCommand(plugin));
        subCommands.put("kick", new ClanKickSubCommand(plugin));
        subCommands.put("role", new ClanRoleSubCommand(plugin));
        subCommands.put("invite", new ClanInviteSubCommand(plugin));
        subCommands.put("join", new ClanJoinSubCommand(plugin));
        subCommands.put("decline", new ClanDeclineSubCommand(plugin));
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer player)) {
            return;
        }

        if (args.length == 0) {
            // help
            return;
        }

        ClanSubCommand subCommand = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subCommand == null) {
            // help
            return;
        }

        String[] subCommandArgs = buildSubCommandArgs(args);
        subCommand.execute(player, subCommandArgs);
    }

    private String[] buildSubCommandArgs(String[] args) {
        return Arrays.stream(args)
            .skip(1)
            .toArray(String[]::new);
    }
}
