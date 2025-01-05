package fr.euphyllia.skylliabank;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skylliabank.commands.BankAdminCommand;
import fr.euphyllia.skylliabank.commands.BankCommand;
import fr.euphyllia.skylliabank.database.MariaDBBankInit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkylliaBank extends JavaPlugin {

    private static BankManager bankManager;
    private static FileConfiguration config;

    public static BankManager getBankManager() {
        return bankManager;
    }

    public static FileConfiguration getConfiguration() {
        return config;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = getConfig();

        // Initialiser l'intégration avec Vault
        if (!EconomyManager.setupEconomy(this)) {
            getLogger().severe("No economy plugin found. The plugin will stop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("Skyllia") == null) {
            getLogger().severe("Skyllia is not installed! The plugin will stop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialiser et charger la base de données
        MariaDBBankInit dbInit = new MariaDBBankInit();
        try {
            if (!dbInit.init()) {
                getLogger().severe("Unable to initialize the bank database! The plugin will stop.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } catch (DatabaseException e) {
            getLogger().severe("Unable to initialize the bank database! The plugin will stop.");
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

        bankManager = new BankManager();

        SkylliaAPI.registerCommands(new BankCommand(this), "bank", "money", "bal", "balance");
        SkylliaAPI.registerAdminCommands(new BankAdminCommand(this), "bank");

        getLogger().info("SkylliaBank has been successfully activated!");
    }
}