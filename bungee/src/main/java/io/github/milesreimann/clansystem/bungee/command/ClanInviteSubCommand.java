package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanInviteSubCommand extends AuthorizedClanSubCommand {
    private final ClanInvitationService clanInvitationService;

    public ClanInviteSubCommand(ClanSystemPlugin plugin) {
        super(plugin);
        clanInvitationService = plugin.getClanInvitationService();
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

        processInviteRequest(player, targetUuid)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processInviteRequest(ProxiedPlayer player, UUID targetUuid) {
        return loadExecutorWithPermissions(player.getUniqueId(), ClanPermissionType.SEND_INVITATION)
            .thenCompose(executor -> validateAndSendInvite(player, executor, targetUuid));
    }

    private CompletionStage<Void> validateAndSendInvite(ProxiedPlayer player, ClanMember executor, UUID targetUuid) {
        if (targetUuid.equals(executor.getUuid())) {
            return failWithMessage("du kannst dich nicht selbst einladen");
        }

        return validateTargetNotInClan(targetUuid)
            .thenCompose(_ -> sendInvite(player, executor, targetUuid));
    }

    private CompletionStage<Boolean> validateTargetNotInClan(UUID targetUuid) {
        return clanMemberService.isInClan(targetUuid)
            .thenCompose(isInClan -> {
                if (Boolean.TRUE.equals(isInClan)) {
                    return failWithMessage("spieler ist bereits in einem clan");
                }

                return CompletableFuture.completedFuture(true);
            });
    }

    private CompletionStage<Void> sendInvite(ProxiedPlayer player, ClanMember executor, UUID targetUuid) {
        return clanInvitationService.sendInvitation(executor.getClan(), executor.getClan(), targetUuid)
            .thenRun(() -> player.sendMessage("einladung versendet"));
    }
}
