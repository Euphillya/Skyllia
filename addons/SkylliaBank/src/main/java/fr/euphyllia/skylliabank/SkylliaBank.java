package fr.euphyllia.skylliabank;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skylliabank.api.BankGenerator;
import fr.euphyllia.skylliabank.commands.BankAdminCommand;
import fr.euphyllia.skylliabank.commands.BankCommand;
import fr.euphyllia.skylliabank.database.mariadb.MariaDBBankInit;
import fr.euphyllia.skylliabank.database.sqlite.SQLiteBankInit;
import fr.euphyllia.skylliabank.listeners.InfoListener;
import fr.euphyllia.skylliabank.papi.SkylliaBankExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkylliaBank extends JavaPlugin {

    private static BankManager bankManager;
    private static SkylliaBank instance;

    public static BankManager getBankManager() {
        return bankManager;
    }

    public static SkylliaBank getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
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

        BankGenerator generator;
        try {
            if (ConfigLoader.database.getMariaDBConfig() != null) {
                MariaDBBankInit dbInit = new MariaDBBankInit();
                if (!dbInit.init()) {
                    getLogger().severe("Unable to initialize the MariaDB bank database! The plugin will stop.");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                generator = MariaDBBankInit.getMariaDbBankGenerator();
            } else if (ConfigLoader.database.getSqLiteConfig() != null) {
                SQLiteBankInit dbInit = new SQLiteBankInit();
                if (!dbInit.init()) {
                    getLogger().severe("Unable to initialize the SQLite bank database! The plugin will stop.");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                generator = SQLiteBankInit.getGenerator();
            } else {
                getLogger().severe("No database configuration found! The plugin will stop.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } catch (DatabaseException exception) {
            getLogger().severe("Unable to initialize the MariaDB bank database! The plugin will stop.");
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(exception);
        }

        bankManager = new BankManager(generator);

        SkylliaAPI.registerCommands(new BankCommand(this), "bank", "money", "bal", "balance");
        SkylliaAPI.registerAdminCommands(new BankAdminCommand(this), "bank");

        getServer().getPluginManager().registerEvents(new InfoListener(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            SkylliaBankExpansion expansion = new SkylliaBankExpansion();
            expansion.init(this);
            expansion.register();
        }

        getLogger().info("SkylliaBank has been successfully activated!");
    }
}