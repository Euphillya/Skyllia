package fr.euphyllia.skylliachallenge.commands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public record ChallengeAdminCommand(SkylliaChallenge plugin) implements SubCommandInterface {


    @Override
    public boolean onCommand(@NotNull Plugin plugin0, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender.hasPermission(permission()) || PermissionImp.hasPermission(sender, permission()))) {
            ConfigLoader.language.sendMessage(sender, "addons.challenge.admin.no-permission");
            return true;
        }
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            ConfigLoader.language.sendMessage(sender, "addons.challenge.admin.unknown-command");
            return true;
        }
        plugin.getChallengeManager().loadChallenges(new File(plugin.getDataFolder(), "challenges"));
        ConfigLoader.language.sendMessage(sender, "addons.challenge.admin.reload-success");
        return true;
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
