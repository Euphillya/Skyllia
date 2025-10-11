package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public record EcoRequirement(double amount) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        Economy eco = getEco();
        if (eco == null) return false;
        return eco.getBalance(player) >= amount;
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
        return eco.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public String getDisplay() {
        return "Avoir " + amount + " en poche (player balance)";
    }
}