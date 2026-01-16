package io.github.milesreimann.clansystem.bungee.validator;

import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Miles R.
 * @since 16.01.2026
 */
@RequiredArgsConstructor
public class ClanNameValidator {
    private final ClanSystemPlugin plugin;

    public boolean validate(ProxiedPlayer player, String clanName) {
        if (clanName.length() < plugin.getConfig().getMinClanNameLength()) {
            plugin.sendMessage(player, "clan-name-too-short");
            return false;
        }

        if (clanName.length() > plugin.getConfig().getMaxClanNameLength()) {
            plugin.sendMessage(player, "clan-name-too-long");
            return false;
        }

        if (!plugin.getConfig().getAllowedClanNamePattern().matcher(clanName).matches()) {
            plugin.sendMessage(player, "clan-name-forbidden-chars");
            return false;
        }

        return true;
    }
}