package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadSubCommands implements SubCommandInterface {


    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PlayerUtils.hasPermission(sender, "skyllia.admins.commands.island.reload")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return;
        }

        ConfigLoader.reloadConfigs();
        ConfigLoader.permissionsV2.compileNow();
        ConfigLoader.islandFlags.compileNow();
        ConfigLoader.language.sendMessage(sender, "island.admin.reload");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}
