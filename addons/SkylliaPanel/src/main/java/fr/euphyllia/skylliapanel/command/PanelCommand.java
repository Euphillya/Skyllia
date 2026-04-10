package fr.euphyllia.skylliapanel.command;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skylliapanel.SkylliaPanelPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PanelCommand implements SubCommandInterface {

    private final SkylliaPanelPlugin plugin;

    public PanelCommand(SkylliaPanelPlugin skylliaPanelPlugin) {
        this.plugin = skylliaPanelPlugin;
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        SubCommandInterface.super.onExecute(plugin, sender, args);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return SubCommandInterface.super.permission();
    }
}
