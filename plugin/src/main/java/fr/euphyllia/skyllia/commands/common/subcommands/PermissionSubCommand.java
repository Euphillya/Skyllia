package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.*;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.configuration.PermissionsToml;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class PermissionSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(PermissionSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.permission")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }
        if (args.length < 4) {
            ConfigLoader.language.sendMessage(player, "island.permission.args-missing");
            return true;
        }
        String permissionsTypeRaw = args[0]; // ISLAND / COMMANDS / INVENTORY
        String roleTypeRaw = args[1]; // ROLE TYPE
        String permissionRaw = args[2]; // Permission
        String valueRaw = args[3]; // true / false

        try {
            PermissionFormat permissionFormat = this.getPermissionFormat(Main.getPlugin(Main.class), player, permissionsTypeRaw, roleTypeRaw, permissionRaw, valueRaw);
            if (permissionFormat == null) return true;
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());
            if (permissionFormat.permissions != null) {

                if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.MANAGE_PERMISSION, false)) {
                    return true;
                }
                if (executorPlayer.getRoleType().getValue() <= permissionFormat.roleType.getValue()) {
                    ConfigLoader.language.sendMessage(player, "island.permission.fail-high-equals-status");
                    return true;
                }

                if (updatePermissions(skyblockManager, island, permissionFormat)) {
                    ConfigLoader.language.sendMessage(player, "island.permission.update.success");
                } else {
                    ConfigLoader.language.sendMessage(player, "island.permission.update.failed");
                }
            } else {
                // RESET !
                if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                    ConfigLoader.language.sendMessage(player, "island.only-owner");
                }
                if (resetPermission(island, permissionFormat)) {
                    ConfigLoader.language.sendMessage(player, "island.permission.update.success");
                } else {
                    ConfigLoader.language.sendMessage(player, "island.permission.update.failed");
                }
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        // ---------- ARG #1 (PermissionsType) ----------
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();

            return Arrays.stream(PermissionsType.values())
                    .map(Enum::name)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .toList();
        }

        // ---------- ARG #2 (RoleType) ----------
        if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();

            return Arrays.stream(RoleType.values())
                    .map(Enum::name)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .toList();
        }

        // ---------- ARG #3 (PermissionsCommandIsland / PermissionsIsland / PermissionsInventory) ----------
        if (args.length == 3) {
            PermissionsType permissionsType;
            try {
                permissionsType = PermissionsType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                return Collections.emptyList();
            }

            List<String> permissionValues = switch (permissionsType) {
                case COMMANDS -> Arrays.stream(PermissionsCommandIsland.values()).map(Enum::name).toList();
                case ISLAND -> Arrays.stream(PermissionsIsland.values()).map(Enum::name).toList();
                case INVENTORY -> Arrays.stream(PermissionsInventory.values()).map(Enum::name).toList();
            };

            List<String> finalValues = new ArrayList<>();
            finalValues.add("RESET");
            finalValues.addAll(permissionValues);

            String partial = args[2].trim().toLowerCase();
            return finalValues.stream()
                    .filter(val -> val.toLowerCase().startsWith(partial))
                    .toList();
        }

        // ---------- ARG #4 (true/false) ----------
        if (args.length == 4) {
            String partial = args[3].trim().toLowerCase();

            return Stream.of("true", "false")
                    .filter(val -> val.startsWith(partial))
                    .toList();
        }

        return Collections.emptyList();
    }

    private PermissionFormat getPermissionFormat(Main main, Entity entity, String permissionsTypeRaw, String roleTypeRaw, String permissionRaw, String valueRaw) {
        PermissionsType permissionsType;
        try {
            permissionsType = PermissionsType.valueOf(permissionsTypeRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            ConfigLoader.language.sendMessage(entity, "island.permission.permission-type-invalid");
            return null;
        }
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(roleTypeRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            ConfigLoader.language.sendMessage(entity, "island.permission.role-invalid");
            return null;
        }
        Permissions permissions = null;
        try {
            if (!permissionRaw.equalsIgnoreCase("RESET")) {
                permissions = switch (permissionsType) {
                    case COMMANDS -> PermissionsCommandIsland.valueOf(permissionRaw.toUpperCase());
                    case ISLAND -> PermissionsIsland.valueOf(permissionRaw.toUpperCase());
                    case INVENTORY -> PermissionsInventory.valueOf(permissionRaw.toUpperCase());
                };
            }
        } catch (IllegalArgumentException e) {
            ConfigLoader.language.sendMessage(entity, "island.permission.permissions-invalid");
            return null;
        }
        boolean value = Boolean.parseBoolean(valueRaw);
        return new PermissionFormat(permissionsType, roleType, permissions, value);
    }

    private boolean updatePermissions(SkyblockManager skyblockManager, Island island, PermissionFormat permissionFormat) {
        PermissionRoleIsland permissionsIsland = skyblockManager.getPermissionIsland(island.getId(), permissionFormat.permissionsType, permissionFormat.roleType).join();
        long flags = permissionsIsland.permission();
        PermissionManager permissionManager = new PermissionManager(flags);
        permissionManager.definePermission(permissionFormat.permissions.getPermissionValue(), permissionFormat.value);
        return island.updatePermission(permissionFormat.permissionsType, permissionFormat.roleType, permissionManager.getPermissions());
    }

    private boolean resetPermission(Island island, PermissionFormat permissionFormat) {
        PermissionsType permissionsType = permissionFormat.permissionsType;
        RoleType roleType = permissionFormat.roleType;
        long value = switch (permissionsType) {
            case INVENTORY -> PermissionsToml.flagsRoleDefaultPermissionInventory.getOrDefault(roleType, 0L);
            case COMMANDS -> PermissionsToml.flagsRoleDefaultPermissionsCommandIsland.getOrDefault(roleType, 0L);
            case ISLAND -> PermissionsToml.flagsRoleDefaultPermissionsIsland.getOrDefault(roleType, 0L);
        };
        return island.updatePermission(permissionsType, roleType, value);
    }

    private record PermissionFormat(PermissionsType permissionsType, RoleType roleType, Permissions permissions,
                                    boolean value) {
    }
}