package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
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
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.cache.island.IslandCreationQueue;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.IslandUtils;
import fr.euphyllia.skyllia.utils.WorldEditUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(CreateSubCommand.class);

    public CompletableFuture<Void> runCreateIsland(Skyllia plugin, Player player, String[] args) {
        return CompletableFuture.runAsync(() -> {

            final UUID playerId = player.getUniqueId();

            if (CommandCacheExecution.isAlreadyExecute(playerId, "create")) {
                ConfigLoader.language.sendMessage(player, "island.generic.command-in-progress");
                return;
            }
            CommandCacheExecution.addCommandExecute(playerId, "create");
            if (!PermissionImp.hasPermission(player, "skyllia.island.command.create")) {
                CommandCacheExecution.removeCommandExec(playerId, "create");
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return;
            }

            try {
                SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                AtomicReference<Island> islandAtomic = new AtomicReference<>(skyblockManager.getIslandByPlayerId(playerId).join());
                if (islandAtomic.get() == null) {
                    List<String> schematicsKeys = ConfigLoader.schematicManager.getIslandTypes();
                    if (schematicsKeys.isEmpty()) {
                        ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                        CommandCacheExecution.removeCommandExec(playerId, "create");
                        return;
                    }
                    String schemKey = (args.length > 0 && schematicsKeys.contains(args[0])) ? args[0] : schematicsKeys.getFirst();
                    Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
                    if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
                        ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                        CommandCacheExecution.removeCommandExec(playerId, "create");
                        return;
                    }
                    IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);

                    if (islandSettings == null) {
                        ConfigLoader.language.sendMessage(player, "island.type-not-exist");
                        CommandCacheExecution.removeCommandExec(playerId, "create");
                        return;
                    }

                    if (!PermissionImp.hasPermission(player, "skyllia.island.command.create.%s".formatted(schemKey))) {
                        ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                        CommandCacheExecution.removeCommandExec(playerId, "create");
                        return;
                    }

                    ConfigLoader.language.sendMessage(player, "island.create-in-progress");
                    UUID idIsland = UUID.randomUUID();
                    boolean isCreate = Boolean.TRUE.equals(skyblockManager.createIsland(idIsland, islandSettings).join());
                    if (!isCreate) {
                        CommandCacheExecution.removeCommandExec(playerId, "create");
                        ConfigLoader.language.sendMessage(player, "island.generic-error");
                        return;
                    }
                    islandAtomic.set(skyblockManager.getIslandByIslandId(idIsland).join());
                    new SkyblockCreateEvent(islandAtomic.get(), playerId).callEvent();

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
                            Location loc = centerPaste.clone();
                            loc.add(0, 0.5, 0);
                            this.addOwnerIslandInMember(islandAtomic.get(), player);
                            player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN)
                                    .thenRun(() -> {
                                        player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                                        player.setFallDistance(0);
                                        plugin.getInterneAPI().getPlayerNMS().setOwnWorldBorder(plugin, player, centerPaste, islandAtomic.get().getSize(), 0, 0);
                                    });
                            new SkyblockLoadEvent(islandAtomic.get()).callEvent();
                            isFirstIteration = false;
                        }
                    }
                    ConfigLoader.language.sendMessage(player, "island.create-finish");
                } else {
                    CommandCacheExecution.removeCommandExec(playerId, "create");
                    new HomeSubCommand().onCommand(plugin, player, args);
                }
            } catch (Exception e) {
                CommandCacheExecution.removeCommandExec(playerId, "create");
                logger.log(Level.WARN, e.getMessage(), e);
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            }
            CommandCacheExecution.removeCommandExec(playerId, "create");
        });
    }


    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (IslandCreationQueue.isQueued(player.getUniqueId())) {
            ConfigLoader.language.sendMessage(player, "island.create.already-in-queue");
            return true;
        }

        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.create")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        boolean bypass = ConfigLoader.general.isAllowBypassIslandQueue()
                && PermissionImp.hasPermission(player, "skyllia.island.bypass.queue");

        if (bypass) {
            runCreateIsland(Skyllia.getInstance(), player, args);
        } else {
            IslandCreationQueue.queuePlayer(player, args);
        }

        return true;
    }


    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            List<String> nameSchem = ConfigLoader.schematicManager.getIslandTypes();
            if (nameSchem.isEmpty()) {
                return Collections.emptyList();
            }

            return nameSchem.stream()
                    .filter(schem -> PermissionImp.hasPermission(sender, "skyllia.island.command.create.%s".formatted(schem)))
                    .filter(schem -> schem.toLowerCase().startsWith(partial))
                    .toList();
        }

        return Collections.emptyList();
    }

    private void pasteSchematic(Skyllia plugin, Island island, Location center, SchematicSetting schematicWorld) {
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
