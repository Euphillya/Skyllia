package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.event.SkyblockWorldInitEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicPlugin;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.IslandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class IslandCreationManager {

    private static final Logger log = LoggerFactory.getLogger(IslandCreationManager.class);
    private final Skyllia plugin;

    public IslandCreationManager(Skyllia plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public Island createIslandForPlayer(@NotNull Player player, @NotNull String schemKey) {
        Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
        if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
            ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
            return null;
        }

        IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);
        if (islandSettings == null) {
            ConfigLoader.language.sendMessage(player, "island.type-not-exist");
            return null;
        }

        UUID islandId = UUID.randomUUID();
        if (!SkylliaAPI.createIsland(islandId, islandSettings)) {
            ConfigLoader.language.sendMessage(player, "island.generic-error");
            return null;
        }

        Island island = SkylliaAPI.getIslandByIslandId(islandId);
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.generic-error");
            return null;
        }

        new SkyblockCreateEvent(island, player.getUniqueId()).callEvent();

        initWorlds(player, island, schematicSettingMap);

        return island;
    }

    private void initWorlds(Player player, Island island, Map<String, SchematicSetting> schematicSettingMap) {
        boolean primaryWorld = true;
        for (Map.Entry<String, SchematicSetting> entry : schematicSettingMap.entrySet()) {
            String worldName = entry.getKey();
            SchematicSetting schematicSetting = entry.getValue();

            Location center = computeCenter(worldName, island, schematicSetting);
            if (center == null) {
                log.warn("World '{}' not loaded — skipping paste for island {}",
                        worldName, island.getId());
                continue;
            }

            pasteSchematic(island, center, schematicSetting);

            new SkyblockWorldInitEvent(island, worldName, center, schematicSetting, primaryWorld)
                    .callEvent();

            if (primaryWorld) {
                setupPrimaryWorld(player, island, center);
                primaryWorld = false;
            }
        }
    }

    private void setupPrimaryWorld(Player player, Island island, Location center) {
        island.addWarps("home", center, true);
        island.updateMember(new Players(
                player.getUniqueId(), player.getName(), island.getId(), RoleType.OWNER));
        teleportPlayer(player, island, center);
        new SkyblockLoadEvent(island).callEvent();
    }

    private void teleportPlayer(Player player, Island island, Location center) {
        Location spawnLoc = center.clone().add(0, 0.5, 0);
        player.teleportAsync(spawnLoc, PlayerTeleportEvent.TeleportCause.PLUGIN)
                .thenRun(() -> {
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    plugin.getInterneAPI()
                            .getPlayerNMS()
                            .setOwnWorldBorder(plugin, player, center, island.getSize(), 0, 0);
                });
    }

    @Nullable
    private Location computeCenter(String worldName, Island island, SchematicSetting schematicSetting) {
        var world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        Location center = RegionHelper.getCenterRegion(
                world,
                island.getPosition().x(),
                island.getPosition().z());
        center.setY(schematicSetting.height());
        return center;
    }

    private void pasteSchematic(Island island, Location center, SchematicSetting schematicSetting) {
        try {
            plugin.getInterneAPI()
                    .getWorldModifier(SchematicPlugin.fromString(schematicSetting.plugin()))
                    .pasteSchematicWE(center, schematicSetting);
        } catch (Exception e) {
            log.error("Failed to paste schematic for island {}: {}", island.getId(), e.getMessage());
            island.setDisable(true);
        }
    }
}
