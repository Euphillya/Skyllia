package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.database.IslandPermissionQuery;
import fr.euphyllia.skyllia.api.permissions.*;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionSubCommand implements SubCommandInterface {

    private static final int BASE = 0;
    private static final List<String> ACTIONS = List.of("list", "set", "toggle");
    private static final List<String> TYPES = List.of("all", "island", "player", "inventory", "commands", "flags");
    private static final List<String> BOOLS = List.of("on", "off", "true", "false");
    private final Logger logger = LogManager.getLogger(PermissionSubCommand.class);

    private final PermissionId PERMISSION_COMMAND_PERMISSION;
    public PermissionSubCommand() {
        this.PERMISSION_COMMAND_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(SkylliaAPI.getPlugin(), "command.island.permission"),
                "Permissions île: gérer les permissions",
                "Autorise à gérer les permissions des rôles de l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        PermissionId permissionId = SkylliaAPI.getPermissionRegistry()
                .getIfPresent(new NamespacedKey(SkylliaAPI.getPlugin(), "block.break"));
        if (permissionId == null) {
            player.sendMessage("Permission 'block.break' non trouvée dans le registre.");
            return true;
        }

        Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            player.sendMessage("Vous n'avez pas d'île associée.");
            return true;
        }


        RoleType roleType = RoleType.VISITOR;
        logger.info("Valeur actuelle avant inversion: " + island.getCompiledPermissions().setFor(roleType).has(permissionId));
        boolean value = !island.getCompiledPermissions().setFor(roleType).has(permissionId);
        logger.info("Valeur après inversion: " + value);

        boolean isUpdated = setDbAndRuntime(island, roleType, permissionId, value);
        player.sendMessage("Mise à jour en base et mémoire: " + (isUpdated ? "réussie" : "échouée"));

        boolean finalValue = island.getCompiledPermissions().has(
                SkylliaAPI.getPermissionRegistry(), roleType, permissionId);
        player.sendMessage("Valeur finale de la permission 'block.break': " + finalValue);

        return true;
    }

    private boolean setDbAndRuntime(Island island, RoleType role, PermissionId pid, boolean value) {
        IslandPermissionQuery query = Skyllia.getInstance().getInterneAPI().getIslandQuery().getIslandPermissionQuery();
        if (query == null) return false;

        boolean success = query.set(island.getId(), role, pid, value);
        if (!success) return false;

        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();
        CompiledPermissions compiled = island.getCompiledPermissions();
        PermissionSet set = compiled.setFor(role);

        if (set != null) {
            set.set(pid, value); // Applique la valeur d'abord
            logger.info("Valeur réglée avant ensureUpToDate: " + set.has(pid));
            compiled.ensureUpToDate(registry);
            logger.info("Valeur après ensureUpToDate: " + set.has(pid));
        }

        compiled.ensureUpToDate(registry);
        return true;
    }

    private boolean canEdit(Player player, Island island) {
        return SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, PERMISSION_COMMAND_PERMISSION);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {

        return Collections.emptyList();
    }
}