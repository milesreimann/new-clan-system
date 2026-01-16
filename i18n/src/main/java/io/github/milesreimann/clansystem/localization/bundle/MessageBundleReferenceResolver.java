package io.github.milesreimann.clansystem.localization.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Miles R.
 * @since 08.01.26
 */
@RequiredArgsConstructor
@Log4j2
public class MessageBundleReferenceResolver {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBundleReferenceResolver.class);
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("%([A-Za-z0-9_.-]+)%");

    private final Properties properties;

    private final Map<String, String> resolvedMessagesCache = new HashMap<>();

    public String resolveReferences(
        String key,
        String message,
        Set<String> visited
    ) {
        String cachedResolvedMessage = resolvedMessagesCache.get(key);
        if (cachedResolvedMessage != null) {
            return cachedResolvedMessage;
        }

        if (!visited.add(key)) {
            LOG.warn("Circular reference detected for key '{}', returning unresolved message", key);
            return message;
        }

        String resolved = replaceReferences(message, visited);
        resolvedMessagesCache.putIfAbsent(key, resolved);

        return resolved;
    }

    private String replaceReferences(String message, Set<String> visited) {
        Matcher matcher = REFERENCE_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String refKey = matcher.group(1);
            String refValue = properties.getProperty(refKey, "%" + refKey + "%");
            refValue = resolveReferences(refKey, refValue, visited);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(refValue));
        }

        matcher.appendTail(builder);

        return builder.toString();
    }
}
