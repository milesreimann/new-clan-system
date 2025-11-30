package io.github.milesreimann.clansystem.bungee.database.config;

import com.zaxxer.hikari.HikariConfig;

/**
 * @author Miles R.
 * @since 27.11.2025
 */
public class DefaultHikariConfig extends HikariConfig {
    private static final String DEFAULT_JDBC_URL = "jdbc:mysql://localhost:3306/clansystem";
    private static final String DEFAULT_USERNAME = "localuser";
    private static final String DEFAULT_PASSWORD = "123456";

    public DefaultHikariConfig() {
        super();
        super.setJdbcUrl(DEFAULT_JDBC_URL);
        super.setUsername(DEFAULT_USERNAME);
        super.setPassword(DEFAULT_PASSWORD);
        super.setMaximumPoolSize(10);
        super.setMinimumIdle(2);
        super.setConnectionTimeout(8000L);
        super.setIdleTimeout(60000L);
        super.setMaxLifetime(600000L);
    }
}
