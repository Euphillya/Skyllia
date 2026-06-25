package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Requirement checking that a given amount of a specific {@link EntityType} is present
 * on the player's island (or, optionally, within a scanning distance of the player).
 *
 * <p>Two modes are supported, controlled by {@code radius}:</p>
 * <ul>
 *     <li><b>Island mode</b> ({@code radius <= 0}) — counts every matching entity located
 *         inside the island's bounds, across every registered Skyblock world.
 *         Example config: {@code "ENTITY:COW 20"}.</li>
 *     <li><b>Scanning-distance mode</b> ({@code radius > 0}) — counts only matching entities
 *         located within {@code radius} blocks of the player. The player must therefore stand
 *         close enough to the entities to validate. Example config: {@code "ENTITY:COW 20 10"}.</li>
 * </ul>
 *
 * <p>Unlike kill/fish/craft requirements, this requirement is evaluated live (it reflects the
 * current world state) and is therefore never tracked through partial progress storage.</p>
 *
 * @param type   the entity type that must be present
 * @param amount the minimum amount of entities required
 * @param radius the scanning distance in blocks, or {@code <= 0} for a full island scan
 */
public record EntityRequirement(EntityType type, int amount, double radius) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        if (radius > 0) {
            return countAroundPlayer(player) >= amount;
        }
        return countOnIsland(island) >= amount;
    }

    private int countAroundPlayer(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return 0;

        if (!SkylliaAPI.isWorldSkyblock(world)) return 0;

        // Le joueur doit être sur sa propre île
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;

        Island islandAtLocation = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (islandAtLocation == null) return 0;
        Island playerIsland = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (playerIsland == null) return 0;
        if (!islandAtLocation.getId().equals(playerIsland.getId())) return 0;

        BoundingBox box = BoundingBox.of(loc, radius, radius, radius);
        return SkylliaAPI.getWorldNMS().getEntities(world, null, box,
                e -> e.getType() == type && islandAtLocation.isInside(e.getLocation())).size();
    }

    private int countOnIsland(Island island) {
        int total = 0;
        for (WorldConfig worldConfig : SkylliaAPI.getRegisteredWorlds()) {
            World world = worldConfig.getWorld();
            if (world == null) continue;

            Location min = island.getMinimumPoint(world);
            Location max = island.getMaximumPoint(world);
            if (min == null || max == null) continue;

            BoundingBox box = BoundingBox.of(min, max);
            List<Entity> entities = SkylliaAPI.getWorldNMS().getEntities(world, null, box,
                    e -> e.getType() == type && island.isInside(e.getLocation()));
            total += entities.size();

            if (total >= amount) return total;
        }
        return total;
    }

    @Override
    public Component getDisplay(Locale locale) {
        if (radius > 0) {
            return ConfigLoader.language.translate(locale, "addons.challenge.requirement.entity.display_radius", Map.of(
                    "%entity_type%", type.name(),
                    "%radius%", String.valueOf((int) radius),
                    "%amount%", String.valueOf(amount)
            ), false);
        }
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.entity.display", Map.of(
                "%entity_type%", type.name(),
                "%amount%", String.valueOf(amount)
        ), false);
    }
}
