package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public record NearEntityRequirement(EntityType type, int amount, double radius) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        Location loc = player.getLocation();
        List<Entity> nearby = (List<Entity>) loc.getWorld().getNearbyEntities(loc, radius, radius, radius, e -> e.getType() == type);
        return nearby.size() >= amount;
    }

    @Override
    public String getDisplay() {
        return "Avoir " + amount + " " + type.name() + " à proximité (" + (int) radius + "m)";
    }
}