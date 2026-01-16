package io.github.milesreimann.clansystem.localization.exception;

/**
 * @author Miles R.
 * @since 08.01.26
 */
public class MessageBundleLoadException extends RuntimeException {
    public MessageBundleLoadException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
