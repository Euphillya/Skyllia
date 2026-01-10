package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadSubCommands implements SubCommandInterface {


    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.reload")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return true;
        }

        ConfigLoader.reloadConfigs();
        ConfigLoader.permissionsV2.compileNow();
        ConfigLoader.language.sendMessage(sender, "island.admin.reload");
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}
