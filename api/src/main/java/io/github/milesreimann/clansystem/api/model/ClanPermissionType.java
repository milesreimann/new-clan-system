package io.github.milesreimann.clansystem.api.model;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public enum ClanPermissionType {
    KICK("clansystem.kick"),
    INVITE("clansystem.invite"),
    ACCEPT_REQUESTS("clansystem.requests.accept"),
    DENY_REQUESTS("clansystem.requests.deny"),
    RENAME_CLAN("clansystem.rename"),
    RETAG_CLAN("clansystem.retag");

    private final String permission;

    ClanPermissionType(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
