package io.github.milesreimann.clansystem.bungee.validator;

import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Miles R.
 * @since 16.01.2026
 */
@RequiredArgsConstructor
public class ClanTagValidator {
    private final ClanSystemPlugin plugin;

    public boolean validate(ProxiedPlayer player, String clanTag) {
        if (clanTag.length() < plugin.getConfig().getMinClanTagLength()) {
            plugin.sendMessage(player, "clan-tag-too-short");
            return false;
        }

        if (clanTag.length() > plugin.getConfig().getMaxClanTagLength()) {
            plugin.sendMessage(player, "clan-tag-too-long");
            return false;
        }

        if (!plugin.getConfig().getAllowedClanTagPattern().matcher(clanTag).matches()) {
            plugin.sendMessage(player, "clan-tag-forbidden-chars");
            return false;
        }

        return true;
    }
}