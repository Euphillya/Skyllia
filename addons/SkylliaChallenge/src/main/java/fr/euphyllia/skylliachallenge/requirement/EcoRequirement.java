package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public record EcoRequirement(int requirementId, NamespacedKey challengeKey, double count) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        Economy eco = getEco();
        if (eco == null) return false;
        return eco.getBalance(player) >= count;
    }

    private Economy getEco() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return null;
        var rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }

    @Override
    public boolean consume(Player player, Island island) {
        Economy eco = getEco();
        if (eco == null) return false;
        return eco.withdrawPlayer(player, count).transactionSuccess();
    }

    @Override
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.vault.display", Map.of(
                "%amount%", String.valueOf(count)
        ), false);
    }
}