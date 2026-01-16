package io.github.milesreimann.clansystem.bungee.command;

import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.model.ClanPermissionType;
import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
import io.github.milesreimann.clansystem.bungee.ClanSystemPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 09.12.25
 */
public class ClanAcceptSubCommand extends AuthorizedClanSubCommand {
    private final ClanJoinRequestService clanJoinRequestService;

    public ClanAcceptSubCommand(ClanSystemPlugin plugin) {
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

        processAcceptRequest(player, targetUuid)
            .exceptionally(exception -> handleError(player, exception));
    }

    private CompletionStage<Void> processAcceptRequest(ProxiedPlayer player, UUID targetUuid) {
        return loadExecutorWithPermissions(player.getUniqueId())
            .thenCompose(executor -> validateAndAcceptRequest(player, executor, targetUuid));
    }

    private CompletionStage<Void> validateAndAcceptRequest(
        ProxiedPlayer player,
        ClanMember executor,
        UUID targetUuid
    ) {
        return clanMemberService.isInClan(targetUuid)
            .thenCompose(isInClan -> {
                if (Boolean.TRUE.equals(isInClan)) {
                    player.sendMessage("spieler ist bereits in eienm clan");
                    return clanJoinRequestService.denyJoinRequest(targetUuid, executor.getClan());
                }

                return acceptJoinRequest(player, executor, targetUuid);
            });
    }

    private CompletionStage<ClanMember> loadExecutorWithPermissions(UUID executorUuid) {
        return clanMemberService.getMemberByUuid(executorUuid)
            .thenCompose(this::validateExecutorInClan)
            .thenCompose(this::validateAcceptPermission);
    }

    private CompletionStage<ClanMember> validateExecutorInClan(ClanMember executor) {
        if (executor == null) {
            return failWithMessage("du bist nicht in einem clan");
        }

        return CompletableFuture.completedStage(executor);
    }

    private CompletionStage<ClanMember> validateAcceptPermission(ClanMember executor) {
        return clanPermissionService.getPermissionByType(ClanPermissionType.ACCEPT_JOIN_REQUEST)
            .thenCompose(permission -> checkPermission(executor, permission))
            .thenApply(_ -> executor);
    }

    private CompletionStage<Void> acceptJoinRequest(ProxiedPlayer player, ClanMember executor, UUID targetUuid) {
        return clanJoinRequestService.acceptJoinRequest(targetUuid, executor.getClan())
            .thenRun(() -> player.sendMessage("anfrage wurde angenommen"));
    }
}
