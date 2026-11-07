package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record NearEntityRequirement(EntityType type, int amount, double radius) implements ChallengeRequirement {

    @Override
    public boolean isMet(final Player player, final Island island) {
        if (amount <= 0) return true;

        final Location loc = player.getLocation();
        final BoundingBox box = BoundingBox.of(loc, radius, radius, radius);

        final List<Entity> nearby = SkylliaAPI.getWorldNMS().getEntities(
                loc.getWorld(),
                player,
                box,
                e -> e.getType() == type && e.isValid()
        );
        return nearby.size() >= amount;
    }

    @Override
    public Component getDisplay(final Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.nearby_entity.display", Map.of(
                "%entity_type%", type.name(),
                "%radius%", String.valueOf((int) radius),
                "%amount%", String.valueOf(amount)
        ), false);
    }
}