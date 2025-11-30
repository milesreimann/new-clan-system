package io.github.milesreimann.clansystem.bungee.service;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.milesreimann.clansystem.api.entity.Clan;
import io.github.milesreimann.clansystem.api.entity.ClanMember;
import io.github.milesreimann.clansystem.api.observer.ClanDeleteObserver;
import io.github.milesreimann.clansystem.api.service.ClanMemberService;
import io.github.milesreimann.clansystem.api.service.ClanRolePermissionService;
import io.github.milesreimann.clansystem.api.service.ClanRoleService;
import io.github.milesreimann.clansystem.api.service.ClanService;
import io.github.milesreimann.clansystem.bungee.entity.ClanImpl;
import io.github.milesreimann.clansystem.bungee.repository.ClanRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public class ClanServiceImpl implements ClanService {
    private final ClanRepository repository;
    private final ClanRoleService clanRoleService;
    private final ClanMemberService clanMemberService;
    private final ClanRolePermissionService clanRolePermissionService;
    private final AsyncLoadingCache<Long, Optional<Clan>> clanByIdCache;
    private final Cache<String, Long> clanNameToIdCache;
    private final Cache<String, Long> clanTagToIdCache;
    private final List<ClanDeleteObserver> deleteObservers;

    public ClanServiceImpl(
        ClanRepository repository,
        ClanRoleService clanRoleService,
        ClanMemberService clanMemberService,
        ClanRolePermissionService clanRolePermissionService
    ) {
        this.repository = repository;
        this.clanRoleService = clanRoleService;
        this.clanMemberService = clanMemberService;
        this.clanRolePermissionService = clanRolePermissionService;

        clanByIdCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .buildAsync((clanId, _) -> repository.findById(clanId)
                .thenApply(Optional::ofNullable)
                .toCompletableFuture());

        clanNameToIdCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

        clanTagToIdCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

        deleteObservers = new CopyOnWriteArrayList<>();
    }

    @Override
    public CompletionStage<Void> createClan(UUID owner, String name, String tag) {
        return repository.insert(new ClanImpl(null, owner, name, tag))
            .thenCompose(clanId -> setupNewClan(clanId, owner))
            .thenApply(_ -> null);
    }

    @Override
    public CompletionStage<Void> deleteClan(Clan clan) {
        return clanMemberService.listMembersByClanId(clan.getId())
            .thenCompose(clanMembers -> repository.deleteById(clan.getId())
                .thenRun(() -> deleteObservers.forEach(observer -> observer.onClanDeleted(clan.getId())))
                .thenCompose(_ -> sendDeleteNotification(clanMembers))
            )
            .thenRun(() -> invalidateClan(clan));
    }

    @Override
    public CompletionStage<Void> renameClan(long clanId, String newName) {
        return repository.updateName(clanId, newName)
            .thenCompose(_ -> sendClanNotification(clanId, "clan name wurde geändert zu " + newName))
            .thenRun(() -> clanByIdCache.synchronous().invalidate(clanId));
    }

    @Override
    public CompletionStage<Void> retagClan(long clanId, String newTag) {
        return repository.updateTag(clanId, newTag)
            .thenCompose(_ -> sendClanNotification(clanId, "clan tag wurde geändert zu " + newTag))
            .thenRun(() -> clanByIdCache.synchronous().invalidate(clanId));
    }

    @Override
    public CompletionStage<Void> sendClanNotification(long clanId, String message) {
        return null;
    }

    @Override
    public CompletionStage<Void> sendClanMessage(long clanId, UUID memberUuid, String message) {
        return null;
    }

    @Override
    public CompletionStage<Clan> getClanById(long clanId) {
        return clanByIdCache.get(clanId)
            .thenApply(optionalClan -> optionalClan.orElse(null));
    }

    @Override
    public CompletionStage<Clan> getClanByName(String name) {
        String key = normalize(name);

        Long id = clanNameToIdCache.getIfPresent(key);
        if (id != null) {
            return getClanById(id);
        }

        return repository.findByName(name)
            .thenApply(clan -> {
                if (clan == null) {
                    return null;
                }

                clanByIdCache.put(clan.getId(), CompletableFuture.completedFuture(Optional.of(clan)));
                clanNameToIdCache.put(key, clan.getId());
                return clan;
            });
    }

    @Override
    public CompletionStage<Clan> getClanByTag(String tag) {
        String key = normalize(tag);

        Long id = clanTagToIdCache.getIfPresent(key);
        if (id != null) {
            return getClanById(id);
        }

        return repository.findByTag(tag)
            .thenApply(clan -> {
                if (clan == null) {
                    return null;
                }

                clanByIdCache.put(clan.getId(), CompletableFuture.completedFuture(Optional.of(clan)));
                clanTagToIdCache.put(key, clan.getId());
                return clan;
            });
    }

    @Override
    public CompletionStage<Boolean> existsClanWithName(String name) {
        return clanNameToIdCache.getIfPresent(normalize(name)) != null
            ? CompletableFuture.completedFuture(true)
            : repository.existsByName(name);
    }

    @Override
    public CompletionStage<Boolean> existsClanWithTag(String tag) {
        return clanTagToIdCache.getIfPresent(normalize(tag)) != null
            ? CompletableFuture.completedFuture(true)
            : repository.existsByTag(tag);
    }

    @Override
    public CompletionStage<List<Clan>> listClans() {
        return repository.findAll();
    }

    @Override
    public void registerDeleteObserver(ClanDeleteObserver observer) {
        deleteObservers.add(observer);
    }

    private CompletionStage<Void> setupNewClan(long clanId, UUID owner) {
        return clanRoleService.createRole(clanId, "Eigentümer", null, 0, true)
            .thenCompose(ownerRole -> clanRolePermissionService.grantAllPermissions(ownerRole.getId())
                .thenCompose(_ -> clanMemberService.joinClan(owner, clanId, ownerRole.getId())));
    }

    private CompletionStage<Void> sendDeleteNotification(List<ClanMember> clanMembers) {
        CompletableFuture<?>[] notifyFutures = clanMembers.stream()
            .map(member -> clanMemberService.notifyMember(member, "Clan wurde gelöscht").toCompletableFuture())
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(notifyFutures);
    }

    private void invalidateClan(Clan clan) {
        if (clan == null) {
            return;
        }

        clanByIdCache.synchronous().invalidate(clan.getId());
        clanNameToIdCache.invalidate(normalize(clan.getName()));
        clanTagToIdCache.invalidate(normalize(clan.getTag()));
    }

    private String normalize(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}
