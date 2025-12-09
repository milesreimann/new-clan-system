package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.observer.ClanDeleteObserver;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.bungee.entity.ClanMemberImpl;
import io.github.milesreimann.clansystem.bungee.listener.ClanMemberCacheInvalidationListener;
import io.github.milesreimann.clansystem.bungee.listener.ClanMemberCacheRemovalListener;
import io.github.milesreimann.clansystem.bungee.repository.ClanMemberRepository;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
public class ClanMemberServiceImpl implements ClanMemberService {
    private final ClanMemberRepository repository;
    private final ClanRoleService clanRoleService;

    private final Map<Long, List<UUID>> clanIdToMemberUuidCache;
    private final AsyncLoadingCache<UUID, Optional<ClanMember>> memberByUuidCache;
    private final AsyncLoadingCache<Long, List<ClanMember>> membersByClanCache;

    @Getter
    private final ClanDeleteObserver clanDeleteObserver;

    public ClanMemberServiceImpl(ClanMemberRepository repository, ClanRoleService clanRoleService) {
        this.repository = repository;
        this.clanRoleService = clanRoleService;

        clanIdToMemberUuidCache = new ConcurrentHashMap<>();

        memberByUuidCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(5_000)
            .removalListener(new ClanMemberCacheRemovalListener(clanIdToMemberUuidCache))
            .buildAsync((memberUuid, _) -> repository.findByUuid(memberUuid)
                .thenApply(Optional::ofNullable)
                .thenApply(optionalClanMember -> {
                    optionalClanMember.ifPresent(clanMember -> clanIdToMemberUuidCache
                        .computeIfAbsent(clanMember.getClan(), _ -> new ArrayList<>())
                        .add(clanMember.getUuid()));
                    return optionalClanMember;
                })
                .toCompletableFuture());

        membersByClanCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(5_000)
            .buildAsync((clanId, _) -> repository.findByClanId(clanId).toCompletableFuture());

        clanDeleteObserver = new ClanMemberCacheInvalidationListener(
            clanIdToMemberUuidCache,
            memberByUuidCache,
            membersByClanCache
        );
    }

    @Override
    public CompletionStage<Void> joinClan(UUID uuid, long clanId, long roleId) {
        return repository.insert(new ClanMemberImpl(uuid, clanId, roleId, null))
            .thenAccept(this::invalidateMember);
    }

    @Override
    public CompletionStage<Void> joinClan(UUID uuid, long clanId) {
        return clanRoleService.getDefaultRoleByClanId(clanId)
            .thenCompose(defaultRole -> joinClan(uuid, clanId, defaultRole.getId()));
    }

    @Override
    public CompletionStage<Void> leaveClan(ClanMember member) {
        return repository.deleteByUuid(member.getUuid())
            .thenRun(() -> invalidateMember(member));
    }

    @Override
    public CompletionStage<Void> updateRole(ClanMember member, long newRoleId) {
        return repository.updateRole(member.getUuid(), newRoleId)
            .thenRun(() -> invalidateMember(member));
    }

    @Override
    public CompletionStage<ClanMember> getMemberByUuid(UUID memberUuid) {
        return memberByUuidCache.get(memberUuid)
            .thenApply(optionalMember -> optionalMember.orElse(null));
    }

    @Override
    public CompletionStage<Boolean> isInClan(UUID memberUuid) {
        CompletionStage<Optional<ClanMember>> memberFuture = memberByUuidCache.getIfPresent(memberUuid);
        if (memberFuture != null) {
            return memberFuture.thenApply(Optional::isPresent);
        }

        return repository.existsByUuid(memberUuid);
    }

    private void invalidateMember(ClanMember member) {
        if (member != null) {
            memberByUuidCache.synchronous().invalidate(member.getUuid());
            membersByClanCache.synchronous().invalidate(member.getClan());
        }
    }
}
