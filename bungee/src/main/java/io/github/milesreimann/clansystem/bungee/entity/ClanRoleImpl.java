package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.ClanRole;
import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
@Data
public class ClanRoleImpl implements ClanRole {
    private final Long id;
    private final Long clan;
    private final String name;
    private final Long parentRole;
    private final Integer sortOrder;
    private final boolean ownerRole;

    @Override
    public CompletionStage<List<String>> getPermissions() {
        return null;
    }

    @Override
    public Optional<Long> getParentRole() {
        return Optional.ofNullable(parentRole);
    }

    @Override
    public Optional<Integer> getSortOrder() {
        return Optional.ofNullable(sortOrder);
    }
}
