package fr.euphyllia.skyfolia.commands;

import fr.euphyllia.skyfolia.commands.subcommands.*;
import org.jetbrains.annotations.NotNull;

public enum SubCommands {
    BIOME(new SetBiomeSubCommand()),
    CREATE(new CreateSubCommand()),
    DELETE(new DeleteSubCommand()),
    PRIVATE(new PrivateSubCommand()),
    TELEPORT(new TeleportSubCommand()),
    TRANSFER(new TransferSubCommand()),
    SETHOME(new SetHomeSubCommand()),
    SETWARP(new SetWarpSubCommand());

    private final SubCommandInterface commandInterface;

    SubCommands(SubCommandInterface subCommandInterface) {
        this.commandInterface = subCommandInterface;
    }

    public static SubCommands subCommandByName(@NotNull String name) {
        for (SubCommands sub : SubCommands.values()) {
            if (sub.name().equalsIgnoreCase(name)) {
                return sub;
            }
        }
        return null;
    }

    public SubCommandInterface getSubCommandInterface() {
        return this.commandInterface;
    }
}