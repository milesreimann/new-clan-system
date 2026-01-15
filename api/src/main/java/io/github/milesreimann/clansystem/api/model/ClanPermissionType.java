package io.github.milesreimann.clansystem.api.model;

/**
 * @author Miles R.
 * @since 30.11.2025
 */
public enum ClanPermissionType {
    KICK_MEMBER("clansystem.member.kick"),
    KICK_MEMBER_BYPASS("clansystem.member.kick.bypass"),
    SEND_INVITATION("clansystem.invitation.send"),
    ACCEPT_JOIN_REQUEST("clansystem.joinrequest.accept"),
    DENY_JOIN_REQUEST("clansystem.joinrequest.deny"),
    RENAME_CLAN("clansystem.rename"),
    RETAG_CLAN("clansystem.retag"),
    CREATE_ROLE("clansystem.role.create"),
    DELETE_ROLE("clansystem.role.delete"),
    INHERIT_ROLE("clansystem.role.inherit"),
    SET_ROLE("clansystem.role.set"),
    SET_ROLE_BYPASS("clansystem.role.set.bypass"),
    MANAGE_ROLES("clansystem.role.manage");

    private final String permission;

    ClanPermissionType(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}