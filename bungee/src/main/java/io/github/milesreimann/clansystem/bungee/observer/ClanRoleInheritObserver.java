package io.github.milesreimann.clansystem.bungee.observer;

/**
 * @author Miles R.
 * @since 05.12.2025
 */
public interface ClanRoleInheritObserver {
    void onClanRoleInherited(long roleId, long inheritsFromRoleId);
}
