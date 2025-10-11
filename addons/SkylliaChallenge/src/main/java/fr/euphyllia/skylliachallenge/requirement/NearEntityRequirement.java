package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record NearEntityRequirement(EntityType type, int amount, double radius) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        Location loc = player.getLocation();
        List<Entity> nearby = (List<Entity>) loc.getWorld().getNearbyEntities(loc, radius, radius, radius, e -> e.getType() == type);
        return nearby.size() >= amount;
    }

    @Override
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.nearby_entity.display", Map.of(
                "%entity_type%", type.name(),
                "%radius%", String.valueOf((int) radius),
                "%amount%", String.valueOf(amount)
        ), false);
    }
}