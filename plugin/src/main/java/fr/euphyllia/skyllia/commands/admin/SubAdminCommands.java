package fr.euphyllia.skyllia.commands.admin;

import fr.euphyllia.skyllia.commands.SubCommandInterface;
import org.jetbrains.annotations.NotNull;

public enum SubAdminCommands {
    ;

    private final SubCommandInterface commandInterface;

    SubAdminCommands(SubCommandInterface subCommandInterface) {
        this.commandInterface = subCommandInterface;
    }

    public static SubAdminCommands subCommandByName(@NotNull String name) {
        for (SubAdminCommands sub : SubAdminCommands.values()) {
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