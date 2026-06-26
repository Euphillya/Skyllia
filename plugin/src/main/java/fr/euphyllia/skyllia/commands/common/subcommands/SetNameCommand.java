package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SetNameCommand implements SubCommandInterface {

    private final PermissionId PERMISSION_SET_NAME;

    public SetNameCommand(Skyllia plugin) {
        this.PERMISSION_SET_NAME = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(plugin, "command.island.set_name"),
                "island.permission.command.set_name.name",
                "island.permission.command.set_name.description"
        ));
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(permission())) {
            ConfigLoader.language.sendMessage(sender, "addons.skylliaextra.player.setname.no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "addons.skylliaextra.player.setname.player-only");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setname.no-island");
            return;
        }

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(
                player, island, PERMISSION_SET_NAME, null,
                ConfigLoader.general.getDebugSettings().permission()
        );
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setname.permission-denied");
            return;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setname.usage");
            return;
        }

        String rawName = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        if (rawName.equalsIgnoreCase("reset")) {
            if (island.setName(null)) {
                ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setname.reset");
            }
            return;
        }

        if (island.setName(rawName)) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setname.success",
                    Map.of("%name%", rawName));
        } else {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setname.failed");
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (args.length == 1) return List.of("reset");
        return List.of();
    }

    @Override
    public String permission() {
        return "skylliaextra.command.setname";
    }
}