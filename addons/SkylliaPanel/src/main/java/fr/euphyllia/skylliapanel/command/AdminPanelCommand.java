package fr.euphyllia.skylliapanel.command;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skylliapanel.SkylliaPanelPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminPanelCommand implements SubCommandInterface {

    private final SkylliaPanelPlugin plugin;

    public AdminPanelCommand(SkylliaPanelPlugin skylliaPanelPlugin) {
        this.plugin = skylliaPanelPlugin;
    }


    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (!sender.hasPermission(permission())) {
            // Todo message - not permission
        }


    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission(permission())) {
            List<String> options = new ArrayList<>(); // Todo : list files
            return options.stream()
                    .filter(s -> s.startsWith(args[0]))
                    .toList();
        }
        return Collections.emptyList();
    }

    @Override
    public String permission() {
        return "skylliapanel.admin";
    }
}
