package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.database.IslandPermissionQuery;
import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionSet;
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

    private static final List<String> ACTIONS = List.of("list", "get", "set", "toggle");
    private static final List<String> BOOLS = List.of("true", "false", "on", "off");

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

        if (!player.hasPermission("skyllia.island.command.permission")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        if (!canEdit(player, island)) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length == 0) {
            ConfigLoader.language.sendMessage(player, "island.permission.usage");
            return true;
        }

        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();

        // /is permission list [filter]
        if (args[0].equalsIgnoreCase("list")) {
            String filter = (args.length >= 2) ? args[1].toLowerCase(Locale.ROOT) : "";
            List<String> all = registry.keys().stream()
                    .map(PermissionSubCommand::toKeyString)
                    .filter(k -> filter.isEmpty() || k.toLowerCase(Locale.ROOT).contains(filter))
                    .sorted()
                    .toList();

            if (all.isEmpty()) {
                ConfigLoader.language.sendMessage(player, "island.permission.list.empty");
                return true;
            }

            // header
            ConfigLoader.language.sendMessage(player, "island.permission.list.header", Map.of(
                    "%count%", String.valueOf(all.size())
            ));

            // limite anti-spam (tu peux ajuster / paginer plus tard)
            int limit = Math.min(all.size(), 80);
            for (int i = 0; i < limit; i++) {
                ConfigLoader.language.sendMessage(player, "island.permission.list.entry", Map.of(
                        "%perm%", all.get(i)
                ));
            }
            if (all.size() > limit) {
                ConfigLoader.language.sendMessage(player, "island.permission.list.more", Map.of(
                        "%more%", String.valueOf(all.size() - limit)
                ));
            }
            return true;
        }

        // Mode actionnel OU forme courte:
        // actionnel:
        //   /is permission get <role> <perm>
        //   /is permission set <role> <perm> <bool>
        //   /is permission toggle <role> <perm>
        //
        // forme courte:
        //   /is permission <role> <perm> [bool]
        int offset = 0;
        String action;

        if (isAction(args[0])) {
            action = args[0].toLowerCase(Locale.ROOT);
            offset = 1;
        } else {
            action = "auto";
        }

        if (args.length - offset < 2) {
            ConfigLoader.language.sendMessage(player, "island.permission.usage");
            return true;
        }

        RoleType role = parseRole(args[offset]);
        if (role == null) {
            ConfigLoader.language.sendMessage(player, "island.permission.role.invalid", Map.of(
                    "%role%", args[offset]
            ));
            return true;
        }

        NamespacedKey key = NamespacedKey.fromString(args[offset + 1]);
        if (key == null) {
            ConfigLoader.language.sendMessage(player, "island.permission.perm.invalid-format", Map.of(
                    "%perm%", args[offset + 1]
            ));
            return true;
        }

        PermissionId pid = registry.getIfPresent(key);
        if (pid == null) {
            ConfigLoader.language.sendMessage(player, "island.permission.perm.unknown", Map.of(
                    "%perm%", toKeyString(key)
            ));
            return true;
        }

        Boolean explicitBool = (args.length - offset >= 3) ? parseBool(args[offset + 2]) : null;

        boolean current = island.getCompiledPermissions().has(registry, role, pid);

        // GET explicite, ou auto sans bool
        if (action.equals("get") || (action.equals("auto") && explicitBool == null)) {
            ConfigLoader.language.sendMessage(player, "island.permission.value", Map.of(
                    "%role%", role.name(),
                    "%perm%", toKeyString(key),
                    "%value%", String.valueOf(current)
            ));
            return true;
        }

        // TOGGLE
        boolean next;
        if (action.equals("toggle")) {
            next = !current;
        } else {
            // SET (explicite ou auto avec bool)
            if (explicitBool == null) {
                ConfigLoader.language.sendMessage(player, "island.permission.bool.invalid", Map.of(
                        "%value%", (args.length - offset >= 3 ? args[offset + 2] : "")
                ));
                return true;
            }
            next = explicitBool;
        }

        boolean updated = setDbAndRuntime(island, role, pid, next);
        if (!updated) {
            ConfigLoader.language.sendMessage(player, "island.permission.update.failed");
            return true;
        }

        boolean finalValue = island.getCompiledPermissions().has(registry, role, pid);
        ConfigLoader.language.sendMessage(player, "island.permission.update.success", Map.of(
                "%role%", role.name(),
                "%perm%", toKeyString(key),
                "%old%", String.valueOf(current),
                "%new%", String.valueOf(finalValue)
        ));
        return true;
    }

    private boolean setDbAndRuntime(Island island, RoleType role, PermissionId pid, boolean value) {
        IslandPermissionQuery query = Skyllia.getInstance()
                .getInterneAPI()
                .getIslandQuery()
                .getIslandPermissionQuery();
        if (query == null) return false;

        boolean success = query.set(island.getId(), role, pid, value);
        if (!success) return false;

        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();
        CompiledPermissions compiled = island.getCompiledPermissions();
        compiled.ensureUpToDate(registry);

        PermissionSet set = compiled.setFor(role);
        if (set == null) return false;

        set.set(pid, value);
        return true;
    }

    private boolean canEdit(Player player, Island island) {
        return SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, PERMISSION_COMMAND_PERMISSION);
    }

    private static boolean isAction(String s) {
        if (s == null) return false;
        String v = s.toLowerCase(Locale.ROOT);
        return ACTIONS.stream().anyMatch(a -> a.equals(v));
    }

    private static RoleType parseRole(String input) {
        if (input == null) return null;
        try {
            return RoleType.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Boolean parseBool(String input) {
        if (input == null) return null;
        String v = input.toLowerCase(Locale.ROOT);
        return switch (v) {
            case "true", "on", "1", "yes" -> true;
            case "false", "off", "0", "no" -> false;
            default -> null;
        };
    }

    private static String toKeyString(NamespacedKey key) {
        return key.getNamespace() + ":" + key.getKey();
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        if (!player.hasPermission("skyllia.island.command.permission")) return Collections.emptyList();

        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();

        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase(Locale.ROOT);
            return ACTIONS.stream()
                    .filter(a -> a.startsWith(partial))
                    .collect(Collectors.toList());
        }

        int offset = isAction(args[0]) ? 1 : 0;

        // rôle
        if (args.length == offset + 1) {
            String partial = args[offset].trim().toLowerCase(Locale.ROOT);
            return Arrays.stream(RoleType.values())
                    .map(Enum::name)
                    .filter(r -> r.toLowerCase(Locale.ROOT).startsWith(partial))
                    .sorted()
                    .toList();
        }

        // permission key
        if (args.length == offset + 2) {
            String partial = args[offset + 1].trim().toLowerCase(Locale.ROOT);
            return registry.keys().stream()
                    .map(PermissionSubCommand::toKeyString)
                    .filter(k -> k.toLowerCase(Locale.ROOT).startsWith(partial))
                    .sorted()
                    .limit(50)
                    .toList();
        }

        // bool uniquement si action != get/list (ou forme courte auto)
        String action = isAction(args[0]) ? args[0].toLowerCase(Locale.ROOT) : "auto";
        if (args.length == offset + 3 && !action.equals("get") && !action.equals("list") && !action.equals("toggle")) {
            String partial = args[offset + 2].trim().toLowerCase(Locale.ROOT);
            return BOOLS.stream().filter(b -> b.startsWith(partial)).toList();
        }

        // si forme courte auto: autoriser le bool en 3e arg
        if (action.equals("auto") && args.length == 3) {
            String partial = args[2].trim().toLowerCase(Locale.ROOT);
            return BOOLS.stream().filter(b -> b.startsWith(partial)).toList();
        }

        return Collections.emptyList();
    }
}
