package io.github.milesreimann.clansystem.api.entity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public interface ClanRole {
    Long getId();

    Long getClan();

    String getName();

    CompletionStage<List<String>> getPermissions();

    Optional<Long> getParentRole();

    Optional<Integer> getSortOrder();

    boolean isOwnerRole();
}