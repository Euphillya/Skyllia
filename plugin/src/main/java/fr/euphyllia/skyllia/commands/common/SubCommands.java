package fr.euphyllia.skyllia.commands.common;

import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.commands.common.subcommands.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum SubCommands {
    ACCESS(new AccessSubCommand(), "access"),
    BAN(new BanSubCommand(), "ban"),
    BIOME(new SetBiomeSubCommand(), "biome"),
    CREATE(new CreateSubCommand(), "create"),
    DELETE(new DeleteSubCommand(), "delete"),
    DELWARP(new DelWarpSubCommand(), "delwarp"),
    DEMOTE(new DemoteSubCommand(), "demote"),
    EXPEL(new ExpelSubCommand(), "expel"),
    GAMERULE(new GameRuleSubCommand(), "gamerule"),
    HOME(new HomeSubCommand(), "home", "go", "tp"),
    INVITE(new InviteSubCommand(), "invite", "add"),
    KICK(new KickSubCommand(), "kick"),
    LEAVE(new LeaveSubCommand(), "leave"),
    PERMISSION(new PermissionSubCommand(), "permission"),
    PROMOTE(new PromoteSubCommand(), "promote"),
    TPS(new TPSSubCommand(), "tps", "lag", "mspt"),
    TRANSFER(new TransferSubCommand(), "transfer"),
    TRUST(new TrustSubCommand(), "trust"),
    SETHOME(new SetHomeSubCommand(), "sethome"),
    SETWARP(new SetWarpSubCommand(), "setwarp"),
    UNBAN(new UnbanSubCommand(), "unban"),
    UNTRUST(new UntrustSubCommand(), "untrust"),
    VISIT(new VisitSubCommand(), "visit"),
    WARP(new WarpSubCommand(), "warp");

    private static final Map<String, SubCommands> commandMap = new HashMap<>();

    static {
        for (SubCommands subCommand : SubCommands.values()) {
            for (String alias : subCommand.aliases) {
                commandMap.put(alias.toLowerCase(), subCommand);
            }
        }
    }

    private final SubCommandInterface commandInterface;
    private final String[] aliases;

    SubCommands(SubCommandInterface subCommandInterface, String... aliases) {
        this.commandInterface = subCommandInterface;
        this.aliases = aliases;
    }

    public static SubCommands subCommandByName(@NotNull String name) {
        return commandMap.get(name.toLowerCase());
    }

    public static Map<String, SubCommands> getCommandMap() {
        return commandMap;
    }

    public SubCommandInterface getSubCommandInterface() {
        return this.commandInterface;
    }
}
