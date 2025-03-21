package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.WorldEditUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SetBiomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetBiomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.biome")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }
        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.biome.args-missing");
            return true;
        }

        String selectBiome = args[0];
        Biome biome;

        InterneAPI api = Main.getPlugin(Main.class).getInterneAPI();
        BiomesImpl biomesImpl = api.getBiomesImpl();

        try {
            biome = api.getBiomesImpl().getBiome(selectBiome);
        } catch (IllegalArgumentException e) {
            ConfigLoader.language.sendMessage(player, "island.biome.not-exist".formatted(selectBiome));
            return true;
        }

        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.biome.%s".formatted(biomesImpl.getNameBiome(biome)))) {
            ConfigLoader.language.sendMessage(player, "island.biome.permission-denied".formatted(selectBiome));
            return true;
        }

        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        if (world == null || !Boolean.TRUE.equals(WorldUtils.isWorldSkyblock(world.getName()))) {
            ConfigLoader.language.sendMessage(player, "island.biome.only-on-island");
            return true;
        }

        try {

            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            final UUID islandId = island.getId();

            if (CommandCacheExecution.isAlreadyExecute(islandId, "biome")) {
                ConfigLoader.language.sendMessage(player, "island.generic.command-in-progress");
                return true;
            }

            CommandCacheExecution.addCommandExecute(islandId, "biome");

            Players executorPlayer = island.getMember(player.getUniqueId());

            if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.SET_BIOME, false)) {
                return true;
            }

            Position islandPosition = island.getPosition();
            player.getScheduler().run(plugin, pScheduler -> {
                Position playerRegionPosition = RegionHelper.getRegionFromChunk(
                        playerLocation.getChunk().getX(), playerLocation.getChunk().getZ());

                if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                    ConfigLoader.language.sendMessage(player, "island.player.not-on-own-island");
                    CommandCacheExecution.removeCommandExec(islandId, "biome");
                    return;
                }

                ConfigLoader.language.sendMessage(player, "island.biome.change-in-progress");

                CompletableFuture<Boolean> changeBiomeFuture;
                String messageToSend;

                if (args.length >= 2 && args[1].equalsIgnoreCase("island")
                        && PermissionImp.hasPermission(player, "skyllia.island.command.biome_island")) {

                    changeBiomeFuture = WorldEditUtils.changeBiomeIsland(world, biome, island);
                    messageToSend = "island.biome.island-success";

                } else {
                    changeBiomeFuture = WorldEditUtils.changeBiomeChunk(player.getChunk(), biome);
                    messageToSend = "island.biome.chunk-success";
                }

                changeBiomeFuture.thenAccept(success -> {
                    if (success) {
                        ConfigLoader.language.sendMessage(player, messageToSend);
                    } else {
                        ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                    }
                    CommandCacheExecution.removeCommandExec(islandId, "biome");
                }).exceptionally(ex -> {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                    ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                    CommandCacheExecution.removeCommandExec(islandId, "biome");
                    return null;
                });
            }, null);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();

            return Main.getPlugin(Main.class).getInterneAPI().getBiomesImpl().getBiomeNameList().stream()
                    .filter(biome -> PermissionImp.hasPermission(sender, "skyllia.island.command.biome.%s".formatted(biome)))
                    .filter(biome -> biome.toLowerCase().startsWith(partial))
                    .toList();
        }

        if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();

            List<String> options = new ArrayList<>();
            options.add("chunk");
            if (PermissionImp.hasPermission(sender, "skyllia.island.command.biome_island")) {
                options.add("island");
            }

            return options.stream()
                    .filter(opt -> opt.toLowerCase().startsWith(partial))
                    .toList();
        }

        return Collections.emptyList();
    }
}
