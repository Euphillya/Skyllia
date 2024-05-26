package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.entity.PlayerFolia;
import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.commands.CacheCommands;
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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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
        PlayerFolia.setGameMode(player, GameMode.SPECTATOR);

        try {
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            AtomicReference<Island> islandAtomic = new AtomicReference<>(skyblockManager.getIslandByOwner(player.getUniqueId()).join());
            if (islandAtomic.get() == null) {
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
                islandAtomic.set(skyblockManager.getIslandByIslandId(idIsland).join());
                CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SkyblockCreateEvent(islandAtomic.get(), player.getUniqueId())));

                boolean isFirstIteration = true;
                for (Map.Entry<String, SchematicSetting> entry : schematicSettingMap.entrySet()) {
                    String worldName = entry.getKey();
                    SchematicSetting schematicSetting = entry.getValue();
                    Location centerPaste = RegionHelper.getCenterRegion(Bukkit.getWorld(worldName), islandAtomic.get().getPosition().x(), islandAtomic.get().getPosition().z());
                    centerPaste.setY(schematicSetting.height());
                    this.pasteSchematic(plugin, islandAtomic.get(), centerPaste, schematicSetting);
                    if (isFirstIteration) {
                        this.setFirstHome(islandAtomic.get(), centerPaste);
                        this.setPermissionsRole(islandAtomic.get());
                        player.teleportAsync(centerPaste, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        PlayerFolia.setGameMode(player, GameMode.SURVIVAL);
                        this.addOwnerIslandInMember(islandAtomic.get(), player);
                        plugin.getInterneAPI().getPlayerNMS().setOwnWorldBorder(plugin, player, centerPaste, islandAtomic.get().getSize(), 0, 0);
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandCreateFinish);
                        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(islandAtomic.get())));
                        isFirstIteration = false;
                    }
                }
            } else {
                new HomeSubCommand().onCommand(plugin, sender, command, label, args);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            PlayerFolia.setGameMode(player, olgGM);
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> nameSchem = new ArrayList<>();
            ConfigToml.schematicWorldMap.forEach((key, schematicWorld) -> {
                if (CacheCommands.createTabCompleteCache.getUnchecked(new CacheCommands.CreateCacheCommandsTabs(sender, key))) {
                    nameSchem.add(key);
                }
            });
            return nameSchem;
        } else {
            return new ArrayList<>();
        }
    }

    private void pasteSchematic(Main plugin, Island island, Location center, SchematicSetting schematicWorld) {
        switch (WorldEditUtils.worldEditVersion()) {
            case WORLD_EDIT ->
                    Bukkit.getRegionScheduler().execute(plugin, center, () -> WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, schematicWorld));
            case FAST_ASYNC_WORLD_EDIT ->
                    Bukkit.getAsyncScheduler().runNow(plugin, task -> WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, schematicWorld));
            case UNDEFINED -> {
                island.setDisable(true);
                throw new UnsupportedOperationException();
            }
        }
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
