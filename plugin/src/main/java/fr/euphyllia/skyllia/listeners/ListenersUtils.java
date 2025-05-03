package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.Permissions;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.island.PlayersInIslandCache;
import fr.euphyllia.skyllia.cache.island.PositionIslandCache;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListenersUtils {

    private ListenersUtils() {

    }

    /**
     * Checks if a given {@link Location} is within an island, validates the specified {@link GameRuleIsland},
     * and cancels the event if needed. If the world is not a Skyblock world, returns {@code null}.
     *
     * @param location    The {@link Location} to check.
     * @param gameRule    The {@link GameRuleIsland} to validate.
     * @param cancellable The event that can be cancelled.
     * @return The {@link Island} if valid, or {@code null} if the location is not in a Skyblock world.
     */
    public static @Nullable Island checkGameRuleIsland(Location location, GameRuleIsland gameRule, Cancellable cancellable) {
        Island island = getIslandFromLocation(location, cancellable);
        if (island == null) {
            return null;
        }

        if (isBlockOutsideIsland(island, location, cancellable)) {
            return island;
        }

        if (PermissionsManagers.testGameRule(gameRule, island)) {
            cancellable.setCancelled(true);
        }
        return island;
    }

    /**
     * Checks if a given {@link Location} is within an island and if the specified {@link Player} has
     * the required {@link Permissions} to perform an action. Cancels the event if the conditions are not met.
     *
     * @param location          The {@link Location} to check.
     * @param player            The {@link Player} performing the action.
     * @param permissionsIsland The {@link Permissions} that need to be validated.
     * @param cancellable       The event that can be cancelled.
     * @return The {@link Island} if valid, or {@code null} if the location is not in a Skyblock world.
     */
    public static @Nullable Island checkPermission(@NotNull Location location, Player player, Permissions permissionsIsland, Cancellable cancellable) {
        Island island = getIslandFromLocation(location, cancellable);
        if (island == null) {
            return null;
        }

        if (isBlockOutsideIsland(island, location, cancellable)) {
            return island;
        }

        Players playersInIsland = PlayersInIslandCache.getPlayers(island.getId(), player.getUniqueId());

        if (!PermissionsManagers.testPermissions(playersInIsland, player, island, permissionsIsland, true)) {
            cancellable.setCancelled(true);
        }
        return island;
    }

    /**
     * Retrieves the {@link Island} from a given {@link Chunk}, cancelling the event if no island is found.
     *
     * @param chunk       The {@link Chunk} to check.
     * @param cancellable The event that can be cancelled.
     * @return The {@link Island} if found, or {@code null} otherwise.
     */
    public static @Nullable Island checkChunkIsIsland(Chunk chunk, Cancellable cancellable) {
        Position position = RegionHelper.getRegionFromChunk(chunk.getX(), chunk.getZ());
        Island island = PositionIslandCache.getIsland(position);
        if (island == null) {
            cancellable.setCancelled(true); // Sécurité !
        }
        return island;
    }

    /**
     * Triggers a {@link PlayerPrepareChangeWorldSkyblockEvent} if the specified world is a Skyblock world.
     *
     * @param player      The {@link Player} transitioning to another world.
     * @param worldConfig
     * @param portalType  The type of portal being used.
     * @param cancellable Event can be canceled
     */
    public static void callPlayerPrepareChangeWorldSkyblockEvent(Player player, WorldConfig worldConfig, PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType, @Nullable Cancellable cancellable) {
        if (cancellable != null) cancellable.setCancelled(true);
        Bukkit.getPluginManager().callEvent(new PlayerPrepareChangeWorldSkyblockEvent(player, worldConfig, portalType));
    }

    /**
     * Checks whether a block is outside the island's boundaries. If it is outside, the event is cancelled.
     *
     * @param island      The {@link Island} in question.
     * @param location    The {@link Location} to check.
     * @param cancellable The event that can be cancelled.
     * @return {@code true} if the block is outside the island boundaries, {@code false} otherwise.
     */
    public static boolean isBlockOutsideIsland(Island island, Location location, Cancellable cancellable) {
        Position origin = island.getPosition();
        boolean outsideIsland = !RegionHelper.isBlockWithinSquare(
                RegionHelper.getCenterRegion(location.getWorld(), origin.x(), origin.z()),
                location.getBlockX(), location.getBlockZ(), island.getSize()
        );
        if (outsideIsland) {
            cancellable.setCancelled(true);
        }
        return outsideIsland;
    }

    /**
     * Utility method that attempts to retrieve an {@link Island} based on a {@link Location}:
     * <ul>
     *     <li>Checks if the world is a Skyblock world</li>
     *     <li>Checks if the chunk corresponds to a valid island</li>
     * </ul>
     *
     * @param location    The {@link Location} to check.
     * @param cancellable The event that can be cancelled.
     * @return The {@link Island} if found, or {@code null} if not a Skyblock world or no island is found.
     */
    private static @Nullable Island getIslandFromLocation(Location location, Cancellable cancellable) {
        Chunk chunk = location.getChunk();
        String worldName = location.getWorld().getName();

        if (!WorldUtils.isWorldSkyblock(worldName)) return null;
        return checkChunkIsIsland(chunk, cancellable);
    }


}
