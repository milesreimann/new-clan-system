package io.github.milesreimann.clansystem.bungee.entity;

import io.github.milesreimann.clansystem.api.entity.ClanRole;
import lombok.Data;

import java.util.Optional;
/**
 * @author Miles R.
 * @since 29.11.2025
 */
@Data
public class ClanRoleImpl implements ClanRole {
    private final Long id;
    private final Long clan;
    private final String name;
    private final Long inheritsFromRole;
    private final Integer sortOrder;

    @Override
    public Optional<Long> getInheritsFromRole() {
        return Optional.ofNullable(inheritsFromRole);
    }

    @Override
    public Optional<Integer> getSortOrder() {
        return Optional.ofNullable(sortOrder);
    }
}
