package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.api.utils.scheduler.SchedulerTask;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.configuration.PermissionsToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.IslandUtils;
import fr.euphyllia.skyllia.utils.WorldEditUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(CreateSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.create")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        GameMode olgGM = player.getGameMode();
        SkylliaAPI.getSchedulerTask().getScheduler(SchedulerTask.SchedulerSoft.MINECRAFT)
                .execute(SchedulerType.ENTITY, player, schedulerTask -> {
                    player.setGameMode(GameMode.SPECTATOR);
                });

        try {
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
            if (island == null) {
                String schemKey = args.length == 0 ? "" : args[0];
                if (schemKey.isEmpty()) {
                    schemKey = ConfigToml.schematicWorldMap.keySet().iterator().next();
                }
                Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
                if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandSchemNotExist);
                    return true;
                }
                IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);

                if (islandSettings == null) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandTypeNotExist);
                    return true;
                }

                if (!player.hasPermission("skyllia.island.command.create.%s".formatted(schemKey))) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                    return true;
                }

                LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandInProgress);
                UUID idIsland = UUID.randomUUID();
                boolean isCreate = Boolean.TRUE.equals(skyblockManager.createIsland(idIsland, islandSettings).join());
                if (!isCreate) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandError);
                    return true;
                }
                island = skyblockManager.getIslandByIslandId(idIsland).join();
                Bukkit.getPluginManager().callEvent(new SkyblockCreateEvent(island, player.getUniqueId()));

                boolean isFirstIteration = true;
                for (Map.Entry<String, SchematicSetting> entry : schematicSettingMap.entrySet()) {
                    String worldName = entry.getKey();
                    SchematicSetting schematicSetting = entry.getValue();
                    Location centerPaste = RegionHelper.getCenterRegion(Bukkit.getWorld(worldName), island.getPosition().x(), island.getPosition().z());
                    centerPaste.setY(schematicSetting.height());
                    this.pasteSchematic(plugin, island, centerPaste, schematicSetting);
                    if (isFirstIteration) {
                        this.setFirstHome(island, centerPaste);
                        this.setPermissionsRole(island);
                        this.teleportPlayerIsland(plugin, player, centerPaste);
                        this.restoreGameMode(plugin, player, GameMode.SURVIVAL);
                        this.addOwnerIslandInMember(island, player);
                        plugin.getInterneAPI().getPlayerNMS().setOwnWorldBorder(plugin, player, centerPaste, island.getSize(), 0, 0);
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandCreateFinish);
                        Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island));
                        isFirstIteration = false;
                    }
                }
            } else {
                new HomeSubCommand().onCommand(plugin, sender, command, label, args);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            this.restoreGameMode(plugin, player, olgGM);
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> nameSchem = new ArrayList<>();
            ConfigToml.schematicWorldMap.forEach((key, schematicWorld) -> nameSchem.add(key));
            return nameSchem;
        } else {
            return new ArrayList<>();
        }
    }

    private void pasteSchematic(Main plugin, Island island, Location center, SchematicSetting schematicWorld) {
        switch (WorldEditUtils.worldEditVersion()) {
            case WORLD_EDIT -> SkylliaAPI.getSchedulerTask().getScheduler(SchedulerTask.SchedulerSoft.MINECRAFT)
                    .execute(SchedulerType.REGION, center, schedulerTask -> {
                        WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, schematicWorld);
                    });
            case FAST_ASYNC_WORLD_EDIT ->
                    WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, schematicWorld);
            case UNDEFINED -> {
                island.setDisable(true); // Désactiver l'ile !
                throw new UnsupportedOperationException();
            }
        }
    }

    private void restoreGameMode(Main plugin, Player player, GameMode gameMode) {
        SkylliaAPI.getSchedulerTask().getScheduler(SchedulerTask.SchedulerSoft.MINECRAFT)
                .execute(SchedulerType.ENTITY, player, schedulerTask -> {
                    player.setGameMode(gameMode);
                });
    }

    private void teleportPlayerIsland(Main plugin, Player player, Location center) {
        SkylliaAPI.getSchedulerTask().getScheduler(SchedulerTask.SchedulerSoft.MINECRAFT)
                .execute(SchedulerType.ENTITY, player, schedulerTask -> {
                    player.teleportAsync(center);
                });
    }

    private boolean setFirstHome(Island island, Location center) {
        return island.addWarps("home", center, true);
    }

    private void addOwnerIslandInMember(Island island, Player player) {
        Players owners = new Players(player.getUniqueId(), player.getName(), island.getId(), RoleType.OWNER);
        island.updateMember(owners);
    }

    private void setPermissionsRole(Island island) {
        for (RoleType roleType : RoleType.values()) {
            island.updatePermission(PermissionsType.ISLAND, roleType, PermissionsToml.flagsRoleDefaultPermissionsIsland.get(roleType));
            island.updatePermission(PermissionsType.COMMANDS, roleType, PermissionsToml.flagsRoleDefaultPermissionsCommandIsland.get(roleType));
            island.updatePermission(PermissionsType.INVENTORY, roleType, PermissionsToml.flagsRoleDefaultPermissionInventory.get(roleType));
        }
    }
}
