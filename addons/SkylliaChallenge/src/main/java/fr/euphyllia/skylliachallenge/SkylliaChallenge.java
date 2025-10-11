package fr.euphyllia.skylliachallenge;

import dev.triumphteam.gui.TriumphGui;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliachallenge.commands.ChallengeAdminCommand;
import fr.euphyllia.skylliachallenge.commands.ChallengeCommand;
import fr.euphyllia.skylliachallenge.gui.GuiSettings;
import fr.euphyllia.skylliachallenge.managers.ChallengeManagers;
import fr.euphyllia.skylliachallenge.storage.InitMariaDB;
import fr.euphyllia.skylliachallenge.storage.InitSQLite;
import fr.euphyllia.skylliachallenge.storage.ProgressStorage;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SkylliaChallenge extends JavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(SkylliaChallenge.class);
    private static SkylliaChallenge instance;
    private ChallengeManagers challengeManager;
    private GuiSettings guiSettings;

    public static SkylliaChallenge getInstance() {
        return instance;
    }

    public ChallengeManagers getChallengeManager() {
        return challengeManager;
    }

    public GuiSettings getGuiSettings() {
        return guiSettings;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        TriumphGui.init(this);

        instance = this;

        getDataFolder().mkdirs();
        getDataFolder().toPath().resolve("challenges").toFile().mkdirs();

        try {
            boolean b = InitMariaDB.initIfConfigured() || InitSQLite.initIfConfigured();
        } catch (Exception exception) {
            log.error("Error during database initialization: {}", exception.getMessage(), exception);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.guiSettings = GuiSettings.load(getConfig());
        this.challengeManager = new ChallengeManagers(this);
        this.challengeManager.loadChallenges(getDataFolder().toPath().resolve("challenges").toFile());

        SkylliaAPI.registerCommands(new ChallengeCommand(this), "challenge");
        SkylliaAPI.registerAdminCommands(new ChallengeAdminCommand(this), "challenge");

        Bukkit.getAsyncScheduler().runAtFixedRate(
                this,
                task -> ProgressStoragePartial.flushDirty(),
                1, 1, TimeUnit.MINUTES
        );

    }

    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        ProgressStoragePartial.shutdown();
        ProgressStorage.shutdown();
    }
}
