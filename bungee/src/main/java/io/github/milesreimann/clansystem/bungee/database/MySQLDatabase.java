package io.github.milesreimann.clansystem.bungee.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.milesreimann.clansystem.bungee.database.config.DefaultHikariConfig;
import io.github.milesreimann.clansystem.bungee.database.model.QueryResult;
import io.github.milesreimann.clansystem.bungee.database.model.QueryRow;
import lombok.extern.java.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Miles R.
 * @since 27.11.2025
 */
@Log
public class MySQLDatabase {
    private final HikariConfig config;

    private HikariDataSource dataSource;
    private ExecutorService executorService;

    private boolean connected = false;

    public MySQLDatabase(HikariConfig config) {
        this.config = config;
    }

    public static MySQLDatabase defaultDatabase() {
        return new MySQLDatabase(new DefaultHikariConfig());
    }

    public void connect() {
        if (!isConnected()) {
            dataSource = new HikariDataSource(config);
            log.info("Connected to MySQL database.");

            executorService = Executors.newCachedThreadPool();
            log.fine("Created new cached thread pool.");

            connected = true;
        }
    }

    public void disconnect() {
        if (isConnected()) {
            executorService.shutdown();
            dataSource.close();
            dataSource = null;
            connected = false;

            log.info("Disconnected from MySQL database.");
        }
    }

    public CompletableFuture<Long> insert(String sql, Object... args) {
        ensureDatabaseIsConnected();

        return CompletableFuture.supplyAsync(
            () -> {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    bindStatementParameters(statement, args);

                    int affectedRows = statement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Insert failed, no rows affected.");
                    }

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Insert failed, no ID obtained.");
                        }

                        return generatedKeys.getLong(1);
                    }
                } catch (SQLException e) {
                    log.log(Level.SEVERE, e, () -> "Failed to execute insert query: " + sql);
                    throw new CompletionException(e);
                }
            },
            executorService
        );
    }

    public CompletableFuture<Integer> update(String sql, Object... args) {
        ensureDatabaseIsConnected();

        return CompletableFuture.supplyAsync(
            () -> {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = createStatement(connection, sql, args)
                ) {
                    return statement.executeUpdate();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, e, () -> "Failed to execute update: " + sql);
                    throw new CompletionException(e);
                }
            },
            executorService
        );
    }

    public CompletionStage<QueryResult> query(String sql, Object... args) {
        ensureDatabaseIsConnected();

        return CompletableFuture.supplyAsync(
            () -> {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = createStatement(connection, sql, args);
                     ResultSet resultSet = statement.executeQuery()
                ) {
                    ResultSetMetaData resultMeta = resultSet.getMetaData();
                    int columnCount = resultMeta.getColumnCount();

                    List<String> columnNames = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        columnNames.add(resultMeta.getColumnName(i));
                    }

                    List<QueryRow> rows = new ArrayList<>();

                    while (resultSet.next()) {
                        Map<String, Object> columnValues = new LinkedHashMap<>(columnCount);

                        for (String columnName : columnNames) {
                            Object value = resultSet.getObject(columnName);
                            columnValues.put(columnName, value);
                        }

                        rows.add(new QueryRow(columnValues));
                    }

                    return new QueryResult(rows);

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            },
            executorService
        );
    }

    private boolean isConnected() {
        return connected;
    }

    private PreparedStatement createStatement(Connection connection, String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        bindStatementParameters(statement, args);
        return statement;
    }

    private void bindStatementParameters(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    private void ensureDatabaseIsConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("Database is not connected.");
        }
    }
}
