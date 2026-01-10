package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpelSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ExpelSubCommand.class);

    private final PermissionId ISLAND_EXPEL_PERMISSION;

    public ExpelSubCommand() {
        this.ISLAND_EXPEL_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.expel"),
                "Expulser un joueur",
                "Autorise à expulser un joueur de l'île"
        ));
    }


    public static void expelPlayer(Skyllia plugin, Island island, Player bPlayerToExpel, Player executor, boolean silent) {
        Location bPlayerExpelLocation = bPlayerToExpel.getLocation();
        if (!WorldUtils.isWorldSkyblock(bPlayerExpelLocation.getWorld().getName())) {
            if (!silent) ConfigLoader.language.sendMessage(executor, "island.expel.player-not-in-island");
            return;
        }

        Bukkit.getRegionScheduler().execute(plugin, bPlayerExpelLocation, () -> {
            int chunkLocX = bPlayerExpelLocation.getChunk().getX();
            int chunkLocZ = bPlayerExpelLocation.getChunk().getZ();

            Position islandPosition = island.getPosition();
            Position playerRegionPosition = RegionHelper.getRegionFromChunk(chunkLocX, chunkLocZ);

            if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                if (!silent) ConfigLoader.language.sendMessage(executor, "island.expel.player-not-in-island");
                return;
            }

            PlayerUtils.teleportPlayerSpawn(bPlayerToExpel);
            ConfigLoader.language.sendMessage(executor, "island.kick.success", Map.of("%player%", bPlayerToExpel.getName()));
        });
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.expel")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }
        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.expel.not-enough-args");
            return true;
        }
        try {
            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());

            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());

            boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_EXPEL_PERMISSION);
            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return true;
            }

            String playerToExpel = args[0];
            Player bPlayerToExpel = Bukkit.getPlayerExact(playerToExpel);
            if (bPlayerToExpel == null) {
                ConfigLoader.language.sendMessage(player, "island.player.not-found");
                return true;
            }
            if (!bPlayerToExpel.isOnline()) {
                ConfigLoader.language.sendMessage(player, "island.player.not-connected");
                return true;
            }
            if (bPlayerToExpel.hasPermission("skyllia.island.command.expel.bypass")) {
                ConfigLoader.language.sendMessage(player, "island.kick.failed");
                return true;
            }

            expelPlayer(Skyllia.getPlugin(Skyllia.class), island, bPlayerToExpel, player, false);

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .map(CommandSender::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
