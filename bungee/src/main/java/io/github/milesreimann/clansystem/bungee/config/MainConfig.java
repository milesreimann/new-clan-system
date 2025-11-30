package io.github.milesreimann.clansystem.bungee.config;

import lombok.Data;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Pattern;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
@Data
public class MainConfig {
    private int minClanNameLength = 5;
    private int maxClanNameLength = 16;
    private String allowedClanNameChars = "[a-zA-Z0-9\\-_$]+";
    private int minClanTagLength = 2;
    private int maxClanTagLength = 8;
    private String allowedClanTagChars = "[a-zA-Z0-9#.,$_\\-]+";

    private Pattern allowedClanNamePattern = Pattern.compile(allowedClanNameChars);
    private Pattern allowedClanTagPattern = Pattern.compile(allowedClanTagChars);

    public boolean isValidClanName(ProxiedPlayer player, String clanName) {
        if (clanName.length() < getMinClanNameLength()) {
            player.sendMessage("name zu kurz");
            return false;
        }

        if (clanName.length() > getMaxClanNameLength()) {
            player.sendMessage("name zu lang");
            return false;
        }

        if (!getAllowedClanNamePattern().matcher(clanName).matches()) {
            player.sendMessage("unerlaubte zeichen im namen");
            return false;
        }

        return true;
    }

    public boolean isValidClanTag(ProxiedPlayer player, String clanTag) {
        if (clanTag.length() < getMinClanTagLength()) {
            player.sendMessage("tag zu kurz");
            return false;
        }

        if (clanTag.length() > getMaxClanNameLength()) {
            player.sendMessage("tag zu lang");
            return false;
        }

        if (!getAllowedClanTagPattern().matcher(clanTag).matches()) {
            player.sendMessage("unerlaubte zeichen im tag");
            return false;
        }

        return true;
    }
}
