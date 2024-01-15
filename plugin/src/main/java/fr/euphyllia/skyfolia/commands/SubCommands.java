package fr.euphyllia.skyfolia.commands;

import fr.euphyllia.skyfolia.commands.subcommands.*;
import org.jetbrains.annotations.NotNull;

public enum SubCommands {
    ACCESS(new AccessSubCommand()),
    BIOME(new SetBiomeSubCommand()),
    CREATE(new CreateSubCommand()),
    DELETE(new DeleteSubCommand()),
    DELWARP(new DelWarpSubCommand()),
    DEMOTE(new DemoteSubCommand()),
    EXPEL(new ExpelSubCommand()),
    HOME(new HomeSubCommand()),
    INVITE(new InviteSubCommand()),
    KICK(new KickSubCommand()),
    LEAVE(new LeaveSubCommand()),
    PROMOTE(new PromoteSubCommand()),
    TRANSFER(new TransferSubCommand()),
    SETHOME(new SetHomeSubCommand()),
    SETWARP(new SetWarpSubCommand()),
    VISIT(new VisitSubCommand()),
    WARP(new WarpSubCommand());

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