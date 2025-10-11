package fr.euphyllia.skylliachallenge.commands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class ChallengeCommand implements SubCommandInterface {

    private final SkylliaChallenge plugin;

    public ChallengeCommand(SkylliaChallenge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin0, @NotNull CommandSender sender, @NotNull String[] args) {
        // /is challenge
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player only.</red>"));
                return true;
            }
            Island island = SkylliaAPI.getCacheIslandByPlayerId(p.getUniqueId());
            if (island == null) {
                p.sendMessage(MiniMessage.miniMessage().deserialize("<red>Vous n'avez pas d'île.</red>"));
                return true;
            }
            plugin.getChallengeManager().openGui(p);
            return true;
        }

        // /is challenge reload
        if ("reload".equalsIgnoreCase(args[0])) {
            if (!PermissionImp.hasPermission(sender, "skyllia.challenge.reload")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Vous n'avez pas la permission.</red>"));
                return true;
            }
            plugin.getChallengeManager().loadChallenges(new File(plugin.getDataFolder(), "challenges"));
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Challenges rechargés.</green>"));
            return true;
        }

        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>/is challenge</gray> <dark_gray>|</dark_gray> <gray>/is challenge reload</gray>"));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload");
        return List.of();
    }
}
