package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import io.github.milesreimann.clansystem.bungee.exception.ClanCommandException;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * @author Miles R.
 * @since 16.01.2026
 */
@RequiredArgsConstructor
public abstract class ClanCommandBase {
    protected final ClanSystemPlugin plugin;

    protected <T> CompletableFuture<T> failWithMessage(String message) {
        return CompletableFuture.failedFuture(new ClanCommandException(message));
    }

    protected Void handleError(ProxiedPlayer player, Throwable throwable) {
        plugin.sendMessage(player, throwable.getMessage());

        if (!(throwable instanceof ClanCommandException)) {
            throwable.printStackTrace();
        }

        return null;
    }
}