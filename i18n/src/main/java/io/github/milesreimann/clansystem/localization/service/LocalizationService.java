package io.github.milesreimann.clansystem.localization.service;

import io.github.milesreimann.clansystem.localization.bundle.MessageBundle;
import io.github.milesreimann.clansystem.localization.bundle.MessageBundleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miles R.
 * @since 08.01.26
 */
public class LocalizationService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalizationService.class);
    
    private static final String LOCALE_FILE_EXTENSION = ".properties";
    private static final String LOCALE_FILE_DELIMITER = "\\.";
    private static final String LOCALE_FILE_SEPARATOR = "_";
    private static final String LOCALE_TAG_SEPARATOR = "-";
    private static final String UNDETERMINED_LANGUAGE_TAG = "und";

    private final Path localesDirectory;
    private final Locale defaultLocale;
    private final MessageBundleLoader messageBundleLoader;

    private final Set<Locale> supportedLocales = ConcurrentHashMap.newKeySet();
    private final Map<Locale, MessageBundle> messageBundles = new ConcurrentHashMap<>();

    public LocalizationService(Path localesDirectory, Locale defaultLocale) {
        this.localesDirectory = localesDirectory;
        this.defaultLocale = defaultLocale;
        messageBundleLoader = new MessageBundleLoader();

        initialize();
    }

    public String getMessage(Locale locale, String key, Object... args) {
        Objects.requireNonNull(key, "Message key cannot be null");

        MessageBundle messageBundle = messageBundles.getOrDefault(locale, messageBundles.get(defaultLocale));

        if (messageBundle == null) {
            LOG.warn(
                "No MessageBundle available for locale '{}' or default locale '{}'. Returning key '{}'.",
                locale, defaultLocale, key
            );
            return key;
        }

        return messageBundle.getMessage(key, args)
            .orElseGet(() -> {
                LOG.warn("Missing message key '{}' for locale '{}'.", key, locale);
                return key;
            });
    }

    public Set<Locale> getSupportedLocales() {
        return Collections.unmodifiableSet(supportedLocales);
    }

    private void initialize() {
        discoverLocales();
        loadMessageBundles();

        LOG.info(
            "Initialized LocalizationService. Loaded {} locales and {} MessageBundles: {}",
            supportedLocales.size(), messageBundles.size(), messageBundles.keySet()
        );
    }

    private void discoverLocales() {
        if (!Files.exists(localesDirectory)) {
            try {
                Files.createDirectories(localesDirectory);
                LOG.debug("Created locales directory: {}", localesDirectory.toAbsolutePath());
            } catch (IOException e) {
                LOG.error("Could not create locales directory: {}", localesDirectory.toAbsolutePath(), e);
                return;
            }
        }

        try (Stream<Path> files = Files.list(localesDirectory)) {
            Set<Locale> locales = files
                .filter(path -> path.endsWith(LOCALE_FILE_EXTENSION))
                .map(this::extractLocaleFromPath)
                .filter(this::isValidLocale)
                .collect(Collectors.toSet());

            if (locales.isEmpty()) {
                LOG.warn("No valid locale files found in directory '{}'", localesDirectory.toAbsolutePath());
                return;
            }

            supportedLocales.addAll(locales);
            LOG.info("Discovered supported locales: {}", supportedLocales);
        } catch (IOException e) {
            LOG.error("Failed to list locale files in directory '{}'", localesDirectory.toAbsolutePath(), e);
        }
    }

    private void loadMessageBundles() {
        if (supportedLocales.isEmpty()) {
            LOG.debug("No supported locales found - skipping MessageBundle loading");
            return;
        }

        if (!supportedLocales.contains(defaultLocale)) {
            LOG.debug("Default locale '{}' not found among supported locales", defaultLocale);
        }

        supportedLocales.forEach(this::loadMessageBundle);
        LOG.info("Finished loading all MessageBundles");
    }

    private void loadMessageBundle(Locale locale) {
        try {
            MessageBundle bundle = messageBundleLoader.load(locale, resolveLocaleFilePath(locale));
            messageBundles.put(locale, bundle);
            LOG.debug("Loaded MessageBundle for locale '{}'", locale);
        } catch (Exception e) {
            LOG.error("Failed to load MessageBundle for locale '{}'. Removing it from supported locales", locale, e);
            supportedLocales.remove(locale);
        }
    }

    private Locale extractLocaleFromPath(Path path) {
        String fileName = path.getFileName().toString();

        String[] parts = fileName.split(LOCALE_FILE_DELIMITER);
        if (parts.length != 2) {
            LOG.warn("Locale file '{}' is invalid", fileName);
            return Locale.ROOT;
        }

        String languageTag = parts[0].replace(LOCALE_FILE_SEPARATOR, LOCALE_TAG_SEPARATOR);
        return Locale.forLanguageTag(languageTag);
    }

    private boolean isValidLocale(Locale locale) {
        return !locale.toLanguageTag().equals(UNDETERMINED_LANGUAGE_TAG);
    }

    private Path resolveLocaleFilePath(Locale locale) {
        String languageTag = locale.toLanguageTag().replace(
            LocalizationService.LOCALE_TAG_SEPARATOR,
            LocalizationService.LOCALE_FILE_SEPARATOR
        );

        String fileName = languageTag + LocalizationService.LOCALE_FILE_EXTENSION;
        return localesDirectory.resolve(fileName);
    }
}
