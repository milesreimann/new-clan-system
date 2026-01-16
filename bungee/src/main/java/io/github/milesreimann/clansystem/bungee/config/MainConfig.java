package io.github.milesreimann.clansystem.bungee.config;

import lombok.Data;

import java.util.Locale;
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
    private Locale defaultLocale = Locale.GERMANY;

    private Pattern allowedClanNamePattern = Pattern.compile(allowedClanNameChars);
    private Pattern allowedClanTagPattern = Pattern.compile(allowedClanTagChars);
}
