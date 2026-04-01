package fr.euphyllia.skyllia.listeners.skyblockevents;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.SkyblockChangeSizeEvent;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkyblockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(SkyblockEvent.class);

    public SkyblockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler
    public void onSkyblockSize(final SkyblockChangeSizeEvent event) {
        Position islandRegion = event.getIsland().getPosition();

        double newSize = event.getNewSize();

        for (WorldConfig worldConfig : WorldUtils.getWorldConfigs()) {
            RegionUtils.getEntitiesInRegion(
                    Skyllia.getInstance(),
                    ConfigLoader.general.getRegionDistance(),
                    EntityType.PLAYER,
                    worldConfig.getWorld(),
                    islandRegion,
                    newSize,
                    entity -> {
                        Player player = (Player) entity;

                        if (player.hasPermission("skyllia.island.worldborder.bypass")) {
                            return;
                        }

                        Location center = RegionHelper.getCenterRegion(worldConfig.getWorld(), islandRegion.x(), islandRegion.z());
                        WorldBorder border = player.getWorldBorder();
                        if (border == null) {
                            border = Bukkit.createWorldBorder();
                        }
                        border.setCenter(center);
                        border.setSize(newSize);
                        player.setWorldBorder(border);
                    }
            );
        }

    }

}