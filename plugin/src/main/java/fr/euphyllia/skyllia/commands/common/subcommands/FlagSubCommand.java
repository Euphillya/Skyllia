package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.database.IslandPermissionQuery;
import fr.euphyllia.skyllia.api.permissions.*;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class FlagSubCommand implements SubCommandInterface {

    private static final List<String> ACTIONS = List.of("list", "get", "set", "toggle");
    private static final List<String> BOOLS = List.of("true", "false", "on", "off");

    private final Logger logger = LogManager.getLogger(FlagSubCommand.class);
    private final PermissionId PERMISSION_COMMAND_FLAG;

    public FlagSubCommand() {
        this.PERMISSION_COMMAND_FLAG = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(SkylliaAPI.getPlugin(), "command.island.flag"),
                "island.permission.command.flag.name",
                "island.permission.command.flag.description"
        ));
    }

    private static boolean isAction(String s) {
        if (s == null) return false;
        String v = s.toLowerCase(Locale.ROOT);
        return ACTIONS.stream().anyMatch(a -> a.equals(v));
    }

    private static Boolean parseBool(String input) {
        if (input == null) return null;
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "true", "on", "1", "yes" -> true;
            case "false", "off", "0", "no" -> false;
            default -> null;
        };
    }

    private static String toKeyString(NamespacedKey key) {
        return key.getNamespace() + ":" + key.getKey();
    }

    private static String flagPermNode(NamespacedKey key) {
        return "skyllia.island.flag." + toKeyString(key);
    }

    private static boolean hasPermissionForFlag(Player player, NamespacedKey key) {
        return PlayerUtils.hasPermission(player, flagPermNode(key));
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }

        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.flag")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return;
        }

        if (!canEdit(player, island)) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        if (args.length == 0) {
            ConfigLoader.language.sendMessage(player, "island.flag.usage");
            return;
        }

        IslandFlagRegistry registry = SkylliaAPI.getFlagRegistry();

        if (args[0].equalsIgnoreCase("list")) {
            String filter = (args.length >= 2) ? args[1].toLowerCase(Locale.ROOT) : "";
            List<String> all = registry.keys().stream()
                    .map(FlagSubCommand::toKeyString)
                    .filter(k -> (filter.isEmpty() || k.toLowerCase(Locale.ROOT).contains(filter)) && PlayerUtils.hasPermission(player, "skyllia.island.flag." + k))
                    .sorted()
                    .toList();

            if (all.isEmpty()) {
                ConfigLoader.language.sendMessage(player, "island.flag.list.empty");
                return;
            }

            ConfigLoader.language.sendMessage(player, "island.flag.list.header", Map.of(
                    "%count%", String.valueOf(all.size())
            ));

            int limit = Math.min(all.size(), 80);
            for (int i = 0; i < limit; i++) {
                ConfigLoader.language.sendMessage(player, "island.flag.list.entry", Map.of(
                        "%flag%", all.get(i)
                ));
            }
            if (all.size() > limit) {
                ConfigLoader.language.sendMessage(player, "island.flag.list.more", Map.of(
                        "%more%", String.valueOf(all.size() - limit)
                ));
            }
            return;
        }

        int offset = 0;
        String action;

        if (isAction(args[0])) {
            action = args[0].toLowerCase(Locale.ROOT);
            offset = 1;
        } else {
            action = "auto";
        }

        if (args.length - offset < 1) {
            ConfigLoader.language.sendMessage(player, "island.flag.usage");
            return;
        }

        NamespacedKey key = NamespacedKey.fromString(args[offset]);
        if (key == null) {
            ConfigLoader.language.sendMessage(player, "island.flag.flag.invalid-format", Map.of(
                    "%flag%", args[offset]
            ));
            return;
        }

        FlagId fid = registry.getIfPresent(key);
        if (fid == null) {
            ConfigLoader.language.sendMessage(player, "island.flag.flag.unknown", Map.of(
                    "%flag%", toKeyString(key)
            ));
            return;
        }

        boolean isReadOnly = action.equals("get")
                || (action.equals("auto") && (args.length - offset < 2 || parseBool(args[offset + 1]) == null));

        if (!isReadOnly && !hasPermissionForFlag(player, key)) {
            ConfigLoader.language.sendMessage(player, "island.flag.permission-denied", Map.of(
                    "%flag%", toKeyString(key)
            ));
            return;
        }

        Boolean explicitBool = (args.length - offset >= 2) ? parseBool(args[offset + 1]) : null;

        boolean current = island.getIslandFlags().has(registry, fid);

        if (action.equals("get") || (action.equals("auto") && explicitBool == null)) {
            ConfigLoader.language.sendMessage(player, "island.flag.value", Map.of(
                    "%flag%", toKeyString(key),
                    "%value%", String.valueOf(current)
            ));
            return;
        }

        boolean next;
        if (action.equals("toggle")) {
            next = !current;
        } else {
            if (explicitBool == null) {
                ConfigLoader.language.sendMessage(player, "island.flag.bool.invalid", Map.of(
                        "%value%", (args.length - offset >= 2 ? args[offset + 1] : "")
                ));
                return;
            }
            next = explicitBool;
        }

        boolean updated = setDbAndRuntime(island, fid, next);
        if (!updated) {
            ConfigLoader.language.sendMessage(player, "island.flag.update.failed");
            return;
        }

        boolean finalValue = island.getIslandFlags().has(registry, fid);
        ConfigLoader.language.sendMessage(player, "island.flag.update.success", Map.of(
                "%flag%", toKeyString(key),
                "%old%", String.valueOf(current),
                "%new%", String.valueOf(finalValue)
        ));
    }


    private boolean setDbAndRuntime(Island island, FlagId fid, boolean value) {
        IslandPermissionQuery query = Skyllia.getInstance()
                .getInterneAPI()
                .getIslandQuery()
                .getIslandPermissionQuery();
        if (query == null) return false;

        IslandFlagRegistry registry = SkylliaAPI.getFlagRegistry();

        // When a ".all" flag is toggled, cascade the value to all children with the same prefix
        FlagNode node = registry.node(fid);
        NamespacedKey key = node.node();
        String keyStr = key.getKey();

        List<FlagId> childFlags = new ArrayList<>();

        if (keyStr.endsWith(".all")) {
            String prefix = keyStr.substring(0, keyStr.length() - 3);
            for (Map.Entry<NamespacedKey, FlagId> entry : registry.entries().entrySet()) {
                NamespacedKey k = entry.getKey();
                if (k.getNamespace().equals(key.getNamespace())
                        && k.getKey().startsWith(prefix)
                        && !k.getKey().equals(keyStr)) {
                    childFlags.add(entry.getValue());
                }
            }
        }

        // Load flags from DB, apply all changes at once, save once
        IslandFlags dbFlags = query.loadIslandFlags(island.getId(), registry);
        if (dbFlags == null) dbFlags = new IslandFlags(registry);

        dbFlags.set(registry, fid, value);
        for (FlagId child : childFlags) {
            dbFlags.set(registry, child, value);
        }

        byte[] blob = PermissionSetCodec.encodeLongs(dbFlags.snapshotWords());
        boolean success = query.saveIslandFlags(island.getId(), blob);
        if (!success) return false;

        // Update runtime cache
        IslandFlags flags = island.getIslandFlags();
        flags.ensureUpToDate(registry);
        flags.set(registry, fid, value);
        for (FlagId child : childFlags) {
            flags.set(registry, child, value);
        }
        return true;
    }

    private boolean canEdit(Player player, Island island) {
        return SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, PERMISSION_COMMAND_FLAG, null, ConfigLoader.general.isDebugPermission());
    }


    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.flag")) return Collections.emptyList();

        IslandFlagRegistry registry = SkylliaAPI.getFlagRegistry();

        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase(Locale.ROOT);
            return ACTIONS.stream()
                    .filter(a -> a.startsWith(partial))
                    .collect(Collectors.toList());
        }

        int offset = isAction(args[0]) ? 1 : 0;
        String action = isAction(args[0]) ? args[0].toLowerCase(Locale.ROOT) : "auto";

        if (args.length == offset + 1) {
            String partial = args[offset].trim().toLowerCase(Locale.ROOT);
            List<String> toSort = new ArrayList<>();
            for (NamespacedKey k : registry.keys()) {
                if (action.equals("get") || action.equals("list") || hasPermissionForFlag(player, k)) {
                    String string = toKeyString(k);
                    if (string.toLowerCase(Locale.ROOT).startsWith(partial)) {
                        toSort.add(string);
                    }
                }
            }
            toSort.sort(null);
            List<String> list = new ArrayList<>();
            long limit = 50;
            for (String keyString : toSort) {
                if (limit-- == 0) break;
                list.add(keyString);
            }
            return list;
        }

        if (args.length == offset + 2 && !action.equals("get") && !action.equals("list") && !action.equals("toggle")) {
            String partial = args[offset + 1].trim().toLowerCase(Locale.ROOT);
            return BOOLS.stream().filter(b -> b.startsWith(partial)).toList();
        }

        if (action.equals("auto") && args.length == 2) {
            String partial = args[1].trim().toLowerCase(Locale.ROOT);
            return BOOLS.stream().filter(b -> b.startsWith(partial)).toList();
        }

        return Collections.emptyList();
    }
}
