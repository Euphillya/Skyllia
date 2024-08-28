package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.*;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.configuration.PermissionsToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(PermissionSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.permission")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 4) {
            LanguageToml.sendMessage(player, LanguageToml.messagePermissionCommandNotEnoughArgs);
            return true;
        }
        String permissionsTypeRaw = args[0]; // ISLAND / COMMANDS / INVENTORY
        String roleTypeRaw = args[1]; // ROLE TYPE
        String permissionRaw = args[2]; // Permission
        String valueRaw = args[3]; // true / false

        try {
            PermissionFormat permissionFormat = this.getPermissionFormat(plugin, player, permissionsTypeRaw, roleTypeRaw, permissionRaw, valueRaw);
            if (permissionFormat == null) return true;
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
            if (island == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());
            if (permissionFormat.permissions != null) {
                if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                    PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                    PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                    if (!permissionManager.hasPermission(PermissionsCommandIsland.MANAGE_PERMISSION)) {
                        LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                        return true;
                    }
                }
                if (executorPlayer.getRoleType().getValue() <= permissionFormat.roleType.getValue()) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePermissionPlayerFailedHighOrEqualsStatus);
                    return true;
                }

                if (updatePermissions(skyblockManager, island, permissionFormat)) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePermissionsUpdateSuccess);
                } else {
                    LanguageToml.sendMessage(player, LanguageToml.messagePermissionsUpdateFailed);
                }
            } else {
                // RESET !
                if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                    LanguageToml.sendMessage(player, LanguageToml.messageOnlyOwner);
                }
                if (resetPermission(island, permissionFormat)) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePermissionsUpdateSuccess);
                } else {
                    LanguageToml.sendMessage(player, LanguageToml.messagePermissionsUpdateFailed);
                }
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.stream(PermissionsType.values()).map(Enum::name).toList();
        }
        if (args.length == 2) {
            return Arrays.stream(RoleType.values()).map(Enum::name).toList();
        }
        if (args.length == 3) {
            PermissionsType permissionsType;
            try {
                permissionsType = PermissionsType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                return new ArrayList<>();
            }
            List<String> permissionValues = new ArrayList<>(switch (permissionsType) {
                case COMMANDS -> Arrays.stream(PermissionsCommandIsland.values()).map(Enum::name).toList();
                case ISLAND -> Arrays.stream(PermissionsIsland.values()).map(Enum::name).toList();
                case INVENTORY -> Arrays.stream(PermissionsInventory.values()).map(Enum::name).toList();
            });
            permissionValues.add(0, "RESET");
            return permissionValues;
        }
        if (args.length == 4) {
            return List.of("true", "false");
        }
        return new ArrayList<>();
    }

    private PermissionFormat getPermissionFormat(Main main, Entity entity, String permissionsTypeRaw, String roleTypeRaw, String permissionRaw, String valueRaw) {
        PermissionsType permissionsType;
        try {
            permissionsType = PermissionsType.valueOf(permissionsTypeRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            LanguageToml.sendMessage(entity, LanguageToml.messagePermissionPermissionTypeInvalid);
            return null;
        }
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(roleTypeRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            LanguageToml.sendMessage(entity, LanguageToml.messagePermissionRoleTypeInvalid);
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
            LanguageToml.sendMessage(entity, LanguageToml.messagePermissionsPermissionsValueInvalid);
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