package io.github.milesreimann.clansystem.bungee.command;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
@RequiredArgsConstructor
public abstract class ClanSubCommand extends ClanCommandBase {
    public abstract void execute(ProxiedPlayer player, String[] args);
}