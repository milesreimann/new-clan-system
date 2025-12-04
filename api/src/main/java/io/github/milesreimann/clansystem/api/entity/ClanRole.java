package io.github.milesreimann.clansystem.api.entity;

import java.util.Optional;

/**
 * @author Miles R.
 * @since 28.11.2025
 */
public interface ClanRole {
    Long getId();

    Long getClan();

    String getName();

    Optional<Long> getInheritsFromRole();

    Optional<Integer> getSortOrder();
}