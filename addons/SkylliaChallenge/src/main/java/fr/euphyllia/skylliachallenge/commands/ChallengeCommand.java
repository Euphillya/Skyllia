package fr.euphyllia.skylliachallenge.commands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChallengeCommand(SkylliaChallenge plugin) implements SubCommandInterface {

    @Override
    public boolean onCommand(@NotNull Plugin plugin0, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            ConfigLoader.language.sendMessage(sender, "addons.challenge.player.player-only");
            return true;
        }
        Island island = SkylliaAPI.getIslandByPlayerId(p.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(sender, "addons.challenge.player.no-island");
            return true;
        }
        plugin.getChallengeManager().openGui(p);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "skyllia.challenge.use";
    }
}
