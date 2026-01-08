package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicPlugin;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SetBiomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetBiomeSubCommand.class);
    private final List<String> biomeNameList = Skyllia.getInstance().getInterneAPI().getBiomesImpl().getBiomeNameList();

    private final PermissionId ISLAND_SET_BIOME_PERMISSION;

    public SetBiomeSubCommand() {
        this.ISLAND_SET_BIOME_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.set_biome"),
                "Changer le biome",
                "Autorise à changer le biome de l'île (chunk / île si autorisé)"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.biome")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.biome.args-missing");
            return true;
        }

        String selectBiome = args[0];
        Biome biome;

        InterneAPI api = Skyllia.getInstance().getInterneAPI();
        BiomesImpl biomesImpl = api.getBiomesImpl();
        biome = api.getBiomesImpl().getBiome(selectBiome);
        if (biome == null) {
            ConfigLoader.language.sendMessage(player, "island.biome.not-exist", Map.of(
                    "%s", selectBiome));
            return true;
        }

        String biomeName = biomesImpl.getNameBiome(biome);
        String biomeRaw = biomeName.split(":")[1];

        if (!player.hasPermission("skyllia.island.command.biome.%s".formatted(biomeRaw))) {
            ConfigLoader.language.sendMessage(player, "island.biome.permission-denied", Map.of("%s", selectBiome));
            return true;
        }

        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        if (world == null || !WorldUtils.isWorldSkyblock(world.getName())) {
            ConfigLoader.language.sendMessage(player, "island.biome.only-on-island");
            return true;
        }

        try {
            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());

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

            boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_SET_BIOME_PERMISSION);
            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                CommandCacheExecution.removeCommandExec(islandId, "biome");
                return true;
            }

            Position islandPosition = island.getPosition();

            Position playerRegionPosition = RegionHelper.getRegionFromLocation(playerLocation);

            if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                ConfigLoader.language.sendMessage(player, "island.player.not-on-own-island");
                CommandCacheExecution.removeCommandExec(islandId, "biome");
                return true;
            }

            ConfigLoader.language.sendMessage(player, "island.biome.change-in-progress");

            CompletableFuture<Boolean> changeBiomeFuture;
            String messageToSend;

            if (args.length >= 2 && args[1].equalsIgnoreCase("island")
                    && player.hasPermission("skyllia.island.command.biome_island")) {

                changeBiomeFuture = Skyllia.getInstance().getInterneAPI().getWorldModifier(SchematicPlugin.UNKNOWN).changeBiomeIsland(world, biome, island, ConfigLoader.general.getRegionDistance());
                messageToSend = "island.biome.island-success";

            } else {
                changeBiomeFuture = Skyllia.getInstance().getInterneAPI().getWorldModifier(SchematicPlugin.UNKNOWN).changeBiomeChunk(player.getLocation(), biome);
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
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        } finally {
            CommandCacheExecution.removeCommandExec(player.getUniqueId(), "biome");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();

            return biomeNameList.stream()
                    .filter(biome -> sender.hasPermission("skyllia.island.command.biome.%s".formatted(biome)))
                    .filter(biome -> biome.toLowerCase().startsWith(partial))
                    .toList();
        }

        if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();

            List<String> options = new ArrayList<>();
            options.add("chunk");
            if (sender.hasPermission("skyllia.island.command.biome_island")) {
                options.add("island");
            }

            return options.stream()
                    .filter(opt -> opt.toLowerCase().startsWith(partial))
                    .toList();
        }

        return Collections.emptyList();
    }
}
