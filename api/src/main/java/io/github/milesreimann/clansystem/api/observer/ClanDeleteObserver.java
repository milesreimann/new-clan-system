package io.github.milesreimann.clansystem.api.observer;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public interface ClanDeleteObserver {
    void onClanDeleted(long clanId);
}