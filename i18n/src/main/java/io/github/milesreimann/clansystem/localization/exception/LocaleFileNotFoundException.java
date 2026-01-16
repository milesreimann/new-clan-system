package io.github.milesreimann.clansystem.localization.exception;

import java.nio.file.Path;
import java.util.Locale;

/**
 * @author Miles R.
 * @since 08.01.26
 */
public class LocaleFileNotFoundException extends RuntimeException {
    public LocaleFileNotFoundException(Locale locale, Path path) {
        super("File for locale '" + locale + "' was not found. Searched at path '" + path.toString() + "'");
    }
}
