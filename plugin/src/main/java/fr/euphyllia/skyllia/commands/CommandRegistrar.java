package fr.euphyllia.skyllia.commands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.commands.admin.SkylliaAdminCommand;
import fr.euphyllia.skyllia.commands.admin.SubAdminCommandImpl;
import fr.euphyllia.skyllia.commands.common.SkylliaCommand;
import fr.euphyllia.skyllia.commands.common.SubCommandImpl;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;

/**
 * This class handles the registration of all plugin commands using Paper's Brigadier.
 */
@SuppressWarnings("UnstableApiUsage")
public class CommandRegistrar {

    private final Skyllia plugin;
    private final SubCommandRegistry commandRegistry;
    private final SubCommandRegistry adminCommandRegistry;

    /**
     * Constructs a CommandRegistrar that will register Skyllia commands.
     *
     * @param plugin The Skyllia plugin instance
     */
    public CommandRegistrar(Skyllia plugin) {
        this.plugin = plugin;

        // Création des SubCommandRegistry
        this.commandRegistry = new SubCommandImpl();
        this.adminCommandRegistry = new SubAdminCommandImpl();
    }

    /**
     * Registers all plugin commands via the Paper lifecycle command event.
     */
    public void registerCommands() {
        LifecycleEventManager<@org.jetbrains.annotations.NotNull Plugin> manager = plugin.getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            commands.register(
                    "skyllia",
                    "Islands commands",
                    java.util.List.of("is", "ob"),
                    new SkylliaCommand(plugin)
            );

            commands.register(
                    "skylliadmin",
                    "island.administrator commands",
                    java.util.List.of("isadmin", "skylliaadmin"),
                    new SkylliaAdminCommand(plugin)
            );
        });
    }

    public SubCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public SubCommandRegistry getAdminCommandRegistry() {
        return adminCommandRegistry;
    }
}