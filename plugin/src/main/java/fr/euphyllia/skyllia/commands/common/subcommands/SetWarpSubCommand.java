package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.island.WarpsInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class SetWarpSubCommand implements SubCommandInterface {

    private static final Logger logger = LogManager.getLogger(SetWarpSubCommand.class);

    private final PermissionId ISLAND_SET_WARP_PERMISSION;

    public SetWarpSubCommand() {
        this.ISLAND_SET_WARP_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.set_warp"),
                "Créer un warp",
                "Autorise à créer/modifier un warp sur l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.warp.args-missing");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.setwarp")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        Location playerLocation = player.getLocation();
        if (!WorldUtils.isWorldSkyblock(playerLocation.getWorld().getName())) {
            ConfigLoader.language.sendMessage(player, "island.player.not-on-island");
            return true;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        String warpName = args[0];

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_SET_WARP_PERMISSION);
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        Position islandPosition = island.getPosition();
        int chunkX = playerLocation.getBlockX() >> 4;
        int chunkZ = playerLocation.getBlockZ() >> 4;
        Position playerRegionPosition = RegionHelper.getRegionFromChunk(
                chunkX,
                chunkZ
        );

        if (!islandPosition.equals(playerRegionPosition)) {
            ConfigLoader.language.sendMessage(player, "island.player.not-on-own-island");
            return true;
        }

        try {
            boolean success = island.addWarps(warpName, playerLocation, false);
            if (success) {
                WarpsInIslandCache.invalidate(island.getId());
                ConfigLoader.language.sendMessage(player, "island.warp.create-success", Map.of("%s", warpName));
            } else {
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return List.of("home", "visit", "...");
    }
}
