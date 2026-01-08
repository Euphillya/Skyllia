package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

// Todo: Implement permission management commands
public class PermissionSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(PermissionSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}