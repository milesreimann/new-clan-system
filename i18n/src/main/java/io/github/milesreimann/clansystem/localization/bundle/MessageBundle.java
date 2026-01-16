package io.github.milesreimann.clansystem.localization.bundle;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Miles R.
 * @since 08.01.26
 */
public class MessageBundle {
    private final Locale locale;
    private final Properties properties;
    private final MessageBundleReferenceResolver referenceResolver;

    private final Map<String, MessageFormat> messageFormatCache = new ConcurrentHashMap<>();

    public MessageBundle(Properties properties, Locale locale) {
        this.properties = properties;
        this.locale = locale;
        referenceResolver = new MessageBundleReferenceResolver(properties);
    }

    public Optional<String> getMessage(String key, Object... args) {
        String rawMessage = properties.getProperty(key);
        if (rawMessage == null) {
            return Optional.empty();
        }

        String resolvedMessage = referenceResolver.resolveReferences(key, rawMessage, new HashSet<>());
        MessageFormat format = messageFormatCache.computeIfAbsent(key, _ -> new MessageFormat(resolvedMessage, locale));

        return Optional.of(format.format(args));
    }
}
