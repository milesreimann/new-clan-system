package io.github.milesreimann.clansystem.api.observer;

/**
 * @author Miles R.
 * @since 05.12.2025
 */
public interface ClanRoleInheritObserver {
    void onClanRoleInherited(long roleId, long inheritsFromRoleId);
}
