package io.github.milesreimann.clansystem.bungee.database.model;

import java.util.Map;

/**
 * @author Miles R.
 * @since 27.11.2025
 */
public record QueryRow(Map<String, Object> columns) {
    public <T> T getOrThrow(String column, Class<T> valueClass) {
        T value = get(column, valueClass);
        if (value == null) {
            throw new NullPointerException("Value of column '" + column + "' is null");
        }

        return value;
    }

    public <T> T get(String column, Class<T> valueClass) {
        return valueClass.cast(columns.get(column));
    }
}
