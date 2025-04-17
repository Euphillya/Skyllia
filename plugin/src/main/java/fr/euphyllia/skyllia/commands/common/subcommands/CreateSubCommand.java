package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
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
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
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
import java.util.concurrent.atomic.AtomicReference;

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(CreateSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (CommandCacheExecution.isAlreadyExecute(player.getUniqueId(), "create")) {
            ConfigLoader.language.sendMessage(player, "island.generic.command-in-progress");
            return true;
        }
        CommandCacheExecution.addCommandExecute(player.getUniqueId(), "create");
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.create")) {
            CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }
        GameMode olgGM = player.getGameMode();

        try {
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            AtomicReference<Island> islandAtomic = new AtomicReference<>(skyblockManager.getIslandByPlayerId(player.getUniqueId()).join());
            if (islandAtomic.get() == null) {
                String schemKey = args.length == 0 ? "" : args[0];
                Set<String> schematicsKeys = ConfigLoader.schematicManager.getSchematics().keySet();
                if (schematicsKeys.isEmpty()) {
                    ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    return true;
                }
                schemKey = schematicsKeys.iterator().next();
                Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
                if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
                    ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    return true;
                }
                IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);

                if (islandSettings == null) {
                    ConfigLoader.language.sendMessage(player, "island.type-not-exist");
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    return true;
                }

                if (!PermissionImp.hasPermission(sender, "skyllia.island.command.create.%s".formatted(schemKey))) {
                    ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    return true;
                }

                ConfigLoader.language.sendMessage(player, "island.create-in-progress");
                UUID idIsland = UUID.randomUUID();
                boolean isCreate = Boolean.TRUE.equals(skyblockManager.createIsland(idIsland, islandSettings).join());
                if (!isCreate) {
                    CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                    ConfigLoader.language.sendMessage(player, "island.generic-error");
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
                        this.addOwnerIslandInMember(islandAtomic.get(), player);
                        Main.getPlugin(Main.class).getInterneAPI().getPlayerNMS().setOwnWorldBorder(Main.getPlugin(Main.class), player, centerPaste, islandAtomic.get().getSize(), 0, 0);
                        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(islandAtomic.get())));
                        isFirstIteration = false;
                    }
                }
                ConfigLoader.language.sendMessage(player, "island.create-finish");
            } else {
                CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
                new HomeSubCommand().onCommand(plugin, sender, args);
            }
        } catch (Exception e) {
            CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
            logger.log(Level.WARN, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
        CommandCacheExecution.removeCommandExec(player.getUniqueId(), "create");
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            List<String> nameSchem = new ArrayList<>();
            ConfigLoader.schematicManager.getSchematics().forEach((islandType, islandSchems) -> {
                if (Boolean.TRUE.equals(CacheCommands.createTabCompleteCache.getIfPresent(new CacheCommands.CreateCacheCommandsTabs(sender, islandType)))
                        && islandType.toLowerCase().startsWith(partial)) {
                    nameSchem.add(islandType);
                }
            });

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
            island.updatePermission(PermissionsType.ISLAND, roleType, ConfigLoader.permissions.getPermissionIsland().get(roleType));
            island.updatePermission(PermissionsType.COMMANDS, roleType, ConfigLoader.permissions.getPermissionsCommands().get(roleType));
            island.updatePermission(PermissionsType.INVENTORY, roleType, ConfigLoader.permissions.getPermissionInventory().get(roleType));
        }
    }
}
