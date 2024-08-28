package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.CommandCacheExecution;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetBiomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetBiomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
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

        Location playerLocation = player.getLocation();
        int chunkLocX = playerLocation.getChunk().getX();
        int chunkLocZ = playerLocation.getChunk().getZ();
        String selectBiome = args[0];

        try {
            Biome biome;
            try {
                biome = Biome.valueOf(selectBiome.toUpperCase());
            } catch (IllegalArgumentException e) {
                LanguageToml.sendMessage(player, LanguageToml.messageBiomeNotExist.formatted(selectBiome));
                return true;
            }

            if (!player.hasPermission("skyllia.island.command.biome.%s".formatted(biome.name()))) {
                LanguageToml.sendMessage(player, LanguageToml.messageBiomePermissionDenied.formatted(selectBiome));
            }

            if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(playerLocation.getWorld().getName()))) {
                LanguageToml.sendMessage(player, LanguageToml.messageBiomeOnlyIsland);
                return true;
            }

            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

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
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                if (!permissionManager.hasPermission(PermissionsCommandIsland.SET_BIOME)) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                    return true;
                }
            }

            Position islandPosition = island.getPosition();
            Position playerRegionPosition = RegionHelper.getRegionInChunk(chunkLocX, chunkLocZ);

            if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerNotInIsland);
                return true;
            }

            World world = player.getWorld();
            LanguageToml.sendMessage(player, LanguageToml.messageBiomeChangeInProgress);

            boolean biomeChanged = WorldEditUtils.changeBiomeChunk(plugin, world, biome, island).join();
            if (biomeChanged) {
                LanguageToml.sendMessage(player, LanguageToml.messageBiomeChangeSuccess);
                CommandCacheExecution.removeCommandExec(islandId, "biome");
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> biomesList = new ArrayList<>();
        if (args.length == 1) {
            for (Biome biome : Biome.values()) {
                if (sender.hasPermission("skyllia.island.command.biome.%s".formatted(biome.name()))) {
                    biomesList.add(biome.name());
                }
            }
        }
        return biomesList;
    }
}
