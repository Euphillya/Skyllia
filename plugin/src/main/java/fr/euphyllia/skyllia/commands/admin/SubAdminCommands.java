package fr.euphyllia.skyllia.commands.admin;

import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.commands.admin.subcommands.ForceDeleteSubCommands;
import fr.euphyllia.skyllia.commands.admin.subcommands.InfoSubCommands;
import fr.euphyllia.skyllia.commands.admin.subcommands.SetIslandTypeSubCommands;
import org.jetbrains.annotations.NotNull;

public enum SubAdminCommands {
    FORCEDELETE(new ForceDeleteSubCommands()),
    INFO(new InfoSubCommands()),
    SETISLANDTYPE(new SetIslandTypeSubCommands()),
    SETSIZE(new SetIslandTypeSubCommands()),
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