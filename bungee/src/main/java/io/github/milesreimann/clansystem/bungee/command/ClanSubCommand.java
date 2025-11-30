package io.github.milesreimann.clansystem.bungee.command;

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public interface ClanSubCommand {
    void execute(ProxiedPlayer player, String[] args);
}
