package fr.euphyllia.skylliachallenge.reward;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * @param commandTemplate ex: "broadcast %player% a fini !"
 */
public record CommandReward(String commandTemplate) implements ChallengeReward {

    @Override
    public void apply(Player player, Island island) {
        String cmd = commandTemplate.replace("%player%", player.getName())
                .replace("%island%", island.getId().toString());
        Bukkit.getGlobalRegionScheduler().execute(SkylliaChallenge.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    @Override
    public Component getDisplay(Locale locale) {
        return null;
    }
}