package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.IslandType;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicWorld;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.IslandUtils;
import fr.euphyllia.skyllia.utils.RegionUtils;
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
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
        player.setGameMode(GameMode.SPECTATOR);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                try {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

                    if (island == null) {
                        SchematicWorld schematicWorld = IslandUtils.getSchematic(args.length == 0 ? null : args[0]);
                        if (schematicWorld == null) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandSchemNotExist);
                            return;
                        }

                        IslandType islandType = IslandUtils.getIslandType(ConfigToml.defaultSchematicKey);

                        if (islandType == null) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandTypeNotExist);
                            return;
                        }

                        if (!player.hasPermission("skyllia.island.command.create.%s".formatted(schematicWorld.key()))) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                            return;
                        }

                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandInProgress);
                        UUID idIsland = UUID.randomUUID();
                        boolean isCreate = Boolean.TRUE.equals(skyblockManager.createIsland(idIsland, islandType).join());
                        if (!isCreate) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandError);
                            return;
                        }
                        island = skyblockManager.getIslandByIslandId(idIsland).join();
                        Bukkit.getPluginManager().callEvent(new SkyblockCreateEvent(island, player.getUniqueId()));

                        Location center = RegionUtils.getCenterRegion(Bukkit.getWorld(schematicWorld.worldName()), island.getPosition().x(), island.getPosition().z());
                        center.setY(schematicWorld.height()); // Fix
                        this.pasteSchematic(plugin, island, center, schematicWorld);
                        this.setFirstHome(island, center);
                        this.restoreGameMode(plugin, player, center);
                        this.addOwnerIslandInMember(island, player);
                        plugin.getInterneAPI().getPlayerNMS().setOwnWorldBorder(plugin, player, center, island.getSize(), 0, 0);
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandCreateFinish);
                        Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island));
                    } else {
                        new HomeSubCommand().onCommand(plugin, sender, command, label, args);
                    }
                } catch (Exception e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
                }
            });
        } finally {
            executor.shutdown();
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

    private void pasteSchematic(Main plugin, Island island, Location center, SchematicWorld schematicWorld) {
        switch (WorldEditUtils.worldEditVersion()) {
            case WORLD_EDIT -> Bukkit.getServer().getRegionScheduler().run(plugin, center, t -> {
                WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, schematicWorld);
            });
            case FAST_ASYNC_WORLD_EDIT ->
                    WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, schematicWorld);
            case UNDEFINED -> {
                island.setDisable(true); // Désactiver l'ile !
                throw new RuntimeException("Unsupported Plugin Paste");
            }
        }
    }

    private void restoreGameMode(Main plugin, Player player, Location center) {
        player.getScheduler().run(plugin, t -> {
            player.teleportAsync(center);
            player.setGameMode(GameMode.SURVIVAL);
        }, null);
    }

    private boolean setFirstHome(Island island, Location center) {
        return island.addWarps("home", center, true);
    }

    private void addOwnerIslandInMember(Island island, Player player) {
        Players owners = new Players(player.getUniqueId(), player.getName(), island.getId(), RoleType.OWNER);
        island.updateMember(owners);
    }
}
