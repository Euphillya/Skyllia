package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
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
import fr.euphyllia.skyllia.cache.CommandCacheExecution;
import fr.euphyllia.skyllia.cache.commands.CacheCommands;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(CreateSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (CommandCacheExecution.isAlreadyExecute(player.getUniqueId(), "create")) {
            LanguageToml.sendMessage(player, LanguageToml.messageCommandAlreadyExecution);
            return true;
        }
        CommandCacheExecution.addCommandExecute(player.getUniqueId(), "create");
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.create")) {
            CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        GameMode olgGM = player.getGameMode();
        if (ConfigToml.changeGameModeWhenTeleportIsland) PlayerFolia.setGameMode(player, GameMode.SPECTATOR);

        try {
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            AtomicReference<Island> islandAtomic = new AtomicReference<>(skyblockManager.getIslandByPlayerId(player.getUniqueId()).join());
            if (islandAtomic.get() == null) {
                String schemKey = args.length == 0 ? "" : args[0];
                if (schemKey.isEmpty()) {
                    schemKey = ConfigToml.schematicWorldMap.keySet().iterator().next();
                }
                Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
                if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
                    LanguageToml.sendMessage(player, LanguageToml.messageIslandSchemNotExist);
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    return true;
                }
                IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);

                if (islandSettings == null) {
                    LanguageToml.sendMessage(player, LanguageToml.messageIslandTypeNotExist);
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    return true;
                }

                if (!PermissionImp.hasPermission(sender, "skyllia.island.command.create.%s".formatted(schemKey))) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    return true;
                }

                LanguageToml.sendMessage(player, LanguageToml.messageIslandInProgress);
                UUID idIsland = UUID.randomUUID();
                boolean isCreate = Boolean.TRUE.equals(skyblockManager.createIsland(idIsland, islandSettings).join());
                if (!isCreate) {
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    LanguageToml.sendMessage(player, LanguageToml.messageIslandError);
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
                    this.pasteSchematic(Main.getPlugin(Main.class), islandAtomic.get(), centerPaste, schematicSetting);
                    if (isFirstIteration) {
                        this.setFirstHome(islandAtomic.get(), centerPaste);
                        this.setPermissionsRole(islandAtomic.get());
                        centerPaste.setY(centerPaste.getY() + 0.5);
                        player.teleportAsync(centerPaste, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        if (ConfigToml.changeGameModeWhenTeleportIsland)
                            PlayerFolia.setGameMode(player, GameMode.SURVIVAL);
                        this.addOwnerIslandInMember(islandAtomic.get(), player);
                        Main.getPlugin(Main.class).getInterneAPI().getPlayerNMS().setOwnWorldBorder(Main.getPlugin(Main.class), player, centerPaste, islandAtomic.get().getSize(), 0, 0);
                        LanguageToml.sendMessage(player, LanguageToml.messageIslandCreateFinish);
                        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(islandAtomic.get())));
                        isFirstIteration = false;
                    }
                }
            } else {
                CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                new HomeSubCommand().onCommand(plugin, sender, args);
            }
        } catch (Exception e) {
            CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
            logger.log(Level.WARN, e.getMessage(), e);
            if (ConfigToml.changeGameModeWhenTeleportIsland) PlayerFolia.setGameMode(player, olgGM);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }
        CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            List<String> nameSchem = new ArrayList<>();
            for (Map.Entry<String, ConcurrentHashMap<String, SchematicSetting>> entry : ConfigToml.schematicWorldMap.entrySet()) {
                String key = entry.getKey();
                if (CacheCommands.createTabCompleteCache.getUnchecked(new CacheCommands.CreateCacheCommandsTabs(sender, key))
                        && key.toLowerCase().startsWith(partial)) {
                    nameSchem.add(key);
                }
            }

            return nameSchem;
        }

        return Collections.emptyList();
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
