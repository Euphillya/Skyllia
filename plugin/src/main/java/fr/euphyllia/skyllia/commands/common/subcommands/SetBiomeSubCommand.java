package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.cache.CommandCacheExecution;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.WorldEditUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SetBiomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetBiomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.biome")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(player, LanguageToml.messageBiomeCommandNotEnoughArgs);
            return true;
        }

        String selectBiome = args[0];
        Biome biome;

        InterneAPI api = Main.getPlugin(Main.class).getInterneAPI();
        BiomesImpl biomesImpl = api.getBiomesImpl();

        try {
            biome = api.getBiomesImpl().getBiome(selectBiome);
        } catch (IllegalArgumentException e) {
            LanguageToml.sendMessage(player, LanguageToml.messageBiomeNotExist.formatted(selectBiome));
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.biome.%s".formatted(biomesImpl.getNameBiome(biome)))) {
            LanguageToml.sendMessage(player, LanguageToml.messageBiomePermissionDenied.formatted(selectBiome));
            return true;
        }

        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        if (world == null || !Boolean.TRUE.equals(WorldUtils.isWorldSkyblock(world.getName()))) {
            LanguageToml.sendMessage(player, LanguageToml.messageBiomeOnlyIsland);
            return true;
        }

        try {

            SkyblockManager skyblockManager = api.getSkyblockManager();
            Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());

            if (island == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            final UUID islandId = island.getId();

            if (CommandCacheExecution.isAlreadyExecute(islandId, "biome")) {
                LanguageToml.sendMessage(player, LanguageToml.messageCommandAlreadyExecution);
                return true;
            }

            CommandCacheExecution.addCommandExecute(islandId, "biome");

            Players executorPlayer = island.getMember(player.getUniqueId());

            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(
                        island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());

                if (!permissionManager.hasPermission(PermissionsCommandIsland.SET_BIOME)) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                    CommandCacheExecution.removeCommandExec(islandId, "biome");
                    return true;
                }
            }

            Position islandPosition = island.getPosition();
            Position playerRegionPosition = RegionHelper.getRegionInChunk(
                    playerLocation.getChunk().getX(), playerLocation.getChunk().getZ());

            if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerNotInIsland);
                CommandCacheExecution.removeCommandExec(islandId, "biome");
                return true;
            }

            LanguageToml.sendMessage(player, LanguageToml.messageBiomeChangeInProgress);

            CompletableFuture<Boolean> changeBiomeFuture;
            String messageToSend;

            if (args.length >= 2 && args[1].equalsIgnoreCase("island")
                    && player.hasPermission("skyllia.island.command.biome_island")) {

                changeBiomeFuture = WorldEditUtils.changeBiomeIsland(world, biome, island);
                messageToSend = LanguageToml.messageBiomeIslandChangeSuccess;

            } else {
                changeBiomeFuture = WorldEditUtils.changeBiomeChunk(player.getChunk(), biome);
                messageToSend = LanguageToml.messageBiomeChangeSuccess;
            }

            changeBiomeFuture.thenAccept(success -> {
                if (success) {
                    LanguageToml.sendMessage(player, messageToSend);
                } else {
                    LanguageToml.sendMessage(player, LanguageToml.messageError);
                }
                CommandCacheExecution.removeCommandExec(islandId, "biome");
            }).exceptionally(ex -> {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                LanguageToml.sendMessage(player, LanguageToml.messageError);
                CommandCacheExecution.removeCommandExec(islandId, "biome");
                return null;
            });
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> biomesList = new ArrayList<>();
            for (String biome : Main.getPlugin(Main.class).getInterneAPI().getBiomesImpl().getBiomeNameList()) {
                if (sender.hasPermission("skyllia.island.command.biome.%s".formatted(biome))) {
                    biomesList.add(biome);
                }
            }
            return biomesList;
        } else if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("chunk");
            if (sender.hasPermission("skyllia.island.command.biome_island")) {
                options.add("island");
            }
            return options;
        }
        return Collections.emptyList();
    }
}
