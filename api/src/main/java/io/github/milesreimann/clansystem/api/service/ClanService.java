package io.github.milesreimann.clansystem.api.service;

import io.github.milesreimann.clansystem.api.entity.Clan;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public interface ClanService {
    CompletionStage<Void> createClan(UUID owner, String name, String tag);

    CompletionStage<Void> deleteClan(Clan clan);

    CompletionStage<Void> renameClan(long clanId, String newName);

    CompletionStage<Void> retagClan(long clanId, String newTag);

    CompletionStage<Clan> getClanById(long clanId);

    CompletionStage<Clan> getClanByName(String name);

    CompletionStage<Clan> getClanByTag(String tag);

    CompletionStage<Boolean> existsClanWithName(String name);

    CompletionStage<Boolean> existsClanWithTag(String tag);
}