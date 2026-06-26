package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SetDescriptionCommand implements SubCommandInterface {

    private final PermissionId PERMISSION_SET_DESCRIPTION;

    public SetDescriptionCommand(Skyllia plugin) {
        this.PERMISSION_SET_DESCRIPTION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(plugin, "command.island.set_description"),
                "island.permission.command.set_description.name",
                "island.permission.command.set_description.description"
        ));
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PlayerUtils.hasPermission(sender, permission())) {
            ConfigLoader.language.sendMessage(sender, "addons.skylliaextra.player.setdescription.no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "addons.skylliaextra.player.setdescription.player-only");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setdescription.no-island");
            return;
        }

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(
                player, island, PERMISSION_SET_DESCRIPTION, null,
                ConfigLoader.general.getDebugSettings().permission()
        );
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setdescription.permission-denied");
            return;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setdescription.usage");
            return;
        }

        String rawDescription = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        if (rawDescription.equalsIgnoreCase("reset")) {
            if (island.setDescription(null)) {
                ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setdescription.reset");
            }
            return;
        }

        if (island.setDescription(rawDescription)) {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setdescription.success",
                    Map.of("%description%", rawDescription));
        } else {
            ConfigLoader.language.sendMessage(player, "addons.skylliaextra.player.setdescription.failed");
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return List.of("reset");
        return List.of();
    }

    @Override
    public String permission() {
        return "skylliaextra.command.setdescription";
    }
}