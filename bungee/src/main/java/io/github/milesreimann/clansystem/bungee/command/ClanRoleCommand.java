package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 04.12.25
 */
@Getter(value = AccessLevel.PROTECTED)
public abstract class ClanRoleCommand extends ClanCommandBase {
    private final ClanPermissionType clanPermissionType;

    public ClanRoleCommand(ClanSystemPlugin plugin, ClanPermissionType clanPermissionType) {
        super(plugin);
        this.clanPermissionType = clanPermissionType;
    }

    public abstract CompletionStage<Void> execute(ProxiedPlayer player, ClanMember clanMember, String[] args);
}
