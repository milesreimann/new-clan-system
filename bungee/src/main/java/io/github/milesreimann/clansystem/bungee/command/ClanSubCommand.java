package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public abstract class ClanSubCommand extends ClanCommandBase {
    public ClanSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
    }

    public abstract void execute(ProxiedPlayer player, String[] args);
}