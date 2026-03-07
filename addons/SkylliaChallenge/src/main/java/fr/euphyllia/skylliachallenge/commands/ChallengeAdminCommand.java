package fr.euphyllia.skylliachallenge.commands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChallengeAdminCommand(SkylliaChallenge plugin) implements SubCommandInterface {


    @Override
    public void onExecute(@NotNull Plugin plugin0, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender.hasPermission(permission()) || sender.hasPermission(permission()))) {
            ConfigLoader.language.sendMessage(sender, "addons.challenge.admin.no-permission");
            return;
        }
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            ConfigLoader.language.sendMessage(sender, "addons.challenge.admin.unknown-command");
            return;
        }
        plugin.reload();
        ConfigLoader.language.sendMessage(sender, "addons.challenge.admin.reload-success");
    }


    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return List.of("reload");
    }

    @Override
    public String permission() {
        return "skyllia.challenge.reload";
    }
}
