package io.github.milesreimann.clansystem.api.entity;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public interface ClanSettings {
    Long getClan();

    boolean canBeRequested();
}