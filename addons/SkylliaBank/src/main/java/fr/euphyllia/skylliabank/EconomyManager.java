package fr.euphyllia.skylliabank;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyManager {
    private static Economy econ = null;

    public static boolean setupEconomy(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault is not installed!");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public static Economy getEconomy() {
        return econ;
    }
}