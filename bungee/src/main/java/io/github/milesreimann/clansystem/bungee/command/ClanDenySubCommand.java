package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanDenySubCommand extends AuthorizedClanSubCommand {
    private final ClanJoinRequestService clanJoinRequestService;

    public ClanDenySubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        clanJoinRequestService = plugin.getClanJoinRequestService();
    }

    @Override
    public void execute(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            // help
            return;
        }

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("ungültige uuid");
            return;
        }

        processDenyRequest(player, targetUuid)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processDenyRequest(ProxiedPlayer player, UUID targetUuid) {
        return loadExecutorWithPermissions(player.getUniqueId(), ClanPermissionType.DENY_JOIN_REQUEST)
            .thenCompose(executor -> denyJoinRequest(player, executor, targetUuid));
    }

    private CompletionStage<Void> denyJoinRequest(ProxiedPlayer player, ClanMember executor, UUID targetUuid) {
        return clanJoinRequestService.denyJoinRequest(targetUuid, executor.getClan())
            .thenRun(() -> player.sendMessage("anfrage abgelehnt"));
    }
}
