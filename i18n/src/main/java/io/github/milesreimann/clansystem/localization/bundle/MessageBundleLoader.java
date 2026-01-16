package io.github.milesreimann.clansystem.localization.bundle;

import io.github.milesreimann.clansystem.localization.exception.LocaleFileNotFoundException;
import io.github.milesreimann.clansystem.localization.exception.MessageBundleLoadException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * @author Miles R.
 * @since 08.01.26
 */
@RequiredArgsConstructor
public class MessageBundleLoader {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBundleLoader.class);

    public MessageBundle load(Locale locale, Path localeFilePath) {
        if (!Files.exists(localeFilePath)) {
            throw new LocaleFileNotFoundException(locale, localeFilePath);
        }

        try {
            Properties properties = loadPropertiesFromFile(localeFilePath);
            MessageBundle messageBundle = new MessageBundle(properties, locale);

            LOG.debug(
                "Loaded MessageBundle for locale '{}' with {} entries from file '{}'",
                locale, properties.size(), localeFilePath.getFileName()
            );

            return messageBundle;
        } catch (IOException e) {
            throw new MessageBundleLoadException("Failed to load MessageBundle for locale '" + locale + "'", e);
        } catch (Exception e) {
            throw new MessageBundleLoadException("Unexpected error while loading the MessageBundle for locale '" + locale + "'", e);
        }
    }

    private Properties loadPropertiesFromFile(Path filePath) throws IOException {
        Properties properties = new Properties();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            properties.load(reader);
            LOG.debug("Successfully read {} properties from file '{}'", properties.size(), filePath.getFileName());
        } catch (IOException e) {
            throw new MessageBundleLoadException("Failed to read file '" + filePath + "'", e);
        }

        return properties;
    }
}
