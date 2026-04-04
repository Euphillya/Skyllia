package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WorldBorderAddEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(WorldBorderAddEvent.class);

    public WorldBorderAddEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAddWorldBorder(final EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        final Location location = player.getLocation();
        final World world = location.getWorld();

        if (world == null || !SkylliaAPI.isWorldSkyblock(world)) {
            return;
        }

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), scheduledTask -> {
            Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
            if (island == null) {
                return;
            }
            if (player.hasPermission("skyllia.island.worldborder.bypass")) {
                return;
            }

            Location centerIsland = RegionHelper.getCenterRegion(world, island.getPosition().x(), island.getPosition().z());

            player.getScheduler().runDelayed(api.getPlugin(), playerTask -> {
                WorldBorder border = player.getWorldBorder();
                if (border == null) {
                    border = Bukkit.createWorldBorder();
                }
                border.setCenter(centerIsland);
                border.setSize(island.getSize());
                player.setWorldBorder(border);
            }, null, 1L);
        });
    }
}