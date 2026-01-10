package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AccessSubCommand implements SubCommandInterface {

    private static final String NODE = "command.island.access";
    private final PermissionId ACCESS_COMMAND_PERMISSION;

    public AccessSubCommand() {
        this.ACCESS_COMMAND_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(SkylliaAPI.getPlugin(), NODE),
                "Accès île: ouvrir/fermer",
                "Autorise à changer l'accès de l'île (public/privé)"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.access")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        boolean hasPermission = SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, ACCESS_COMMAND_PERMISSION);

        if (!hasPermission) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        boolean statusAccessUpdate = !island.isPrivateIsland();
        boolean isUpdate = island.setPrivateIsland(statusAccessUpdate);

        if (isUpdate) {
            if (statusAccessUpdate) {
                ConfigLoader.language.sendMessage(player, "island.access.close");
                ConfigLoader.worldManager.getWorldConfigs().forEach((name, environnements) -> {
                    RegionUtils.getEntitiesInRegion(
                            Skyllia.getPlugin(Skyllia.class),
                            ConfigLoader.general.getRegionDistance(),
                            EntityType.PLAYER,
                            Bukkit.getWorld(name),
                            island.getPosition(),
                            island.getSize(),
                            entity -> {
                                Player playerInIsland = (Player) entity;

                                // NOTE: ton code d'origine check la permission sur "player" (le sender),
                                // pas sur "playerInIsland". Je garde le même comportement.
                                if (!player.hasPermission("skyllia.island.command.access.bypass")) return;

                                Bukkit.getAsyncScheduler().runNow(Skyllia.getPlugin(Skyllia.class), t -> {
                                    Players players = island.getMember(playerInIsland.getUniqueId());
                                    if (players == null
                                            || players.getRoleType().equals(RoleType.BAN)
                                            || players.getRoleType().equals(RoleType.VISITOR)) {
                                        PlayerUtils.teleportPlayerSpawn(playerInIsland);
                                    }
                                });
                            }
                    );
                });
            } else {
                ConfigLoader.language.sendMessage(player, "island.access.open");
            }
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}