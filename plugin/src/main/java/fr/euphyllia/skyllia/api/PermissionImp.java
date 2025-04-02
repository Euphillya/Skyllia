package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.configuration.manager.GeneralConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

/**
 * This class provides utility methods to check permissions for an
 * {@link org.bukkit.entity.Entity} or a {@link org.bukkit.command.CommandSender}.
 * <p>
 * It uses the debug setting defined in {@link ConfigToml} to display debug
 * information about permissions when enabled.
 */
public class PermissionImp {

    /**
     * Logger used to display debugging or error information related to the use
     * of this class.
     */
    private static final Logger log = LogManager.getLogger(PermissionImp.class);

    /**
     * Checks whether an {@link Entity} has a specific permission.
     * <p>
     * If the debug option {@link GeneralConfigManager#isDebugPermission()} is enabled,
     * a message showing the permission name and its value (true/false) will be logged.
     *
     * @param entity      The entity for which you want to check the permission.
     * @param permissions The permission to check.
     * @return {@code true} if the entity has the permission, {@code false} otherwise.
     */
    public static boolean hasPermission(Entity entity, String permissions) {
        boolean hasPerm = entity.hasPermission(permissions);
        if (ConfigLoader.general.isDebugPermission()) {
            debugPermissionCheck(permissions, hasPerm);
        }
        return hasPerm;
    }

    /**
     * Checks whether a {@link CommandSender} has a specific permission.
     * <p>
     * If the debug option {@link GeneralConfigManager#isDebugPermission()} is enabled,
     * a message showing the permission name and its value (true/false) will be logged.
     *
     * @param sender      The sender of a command (e.g., console, player).
     * @param permissions The permission to check.
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     */
    public static boolean hasPermission(CommandSender sender, String permissions) {
        boolean hasPerm = sender.hasPermission(permissions);
        if (ConfigLoader.general.isDebugPermission()) {
            debugPermissionCheck(permissions, hasPerm);
        }
        return hasPerm;
    }

    /**
     * Logs a debug message indicating the permission name and whether the entity
     * or sender has that permission or not.
     *
     * @param permissions The name of the permission in question.
     * @param hasPerm     The value of the permission (true if granted, false otherwise).
     */
    private static void debugPermissionCheck(String permissions, boolean hasPerm) {
        log.debug("PermissionName : {} - Value: {}", permissions, hasPerm);
    }
}
