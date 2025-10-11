package fr.euphyllia.skylliachallenge;

import dev.triumphteam.gui.TriumphGui;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliachallenge.commands.ChallengeCommand;
import fr.euphyllia.skylliachallenge.managers.ChallengeManagers;
import fr.euphyllia.skylliachallenge.storage.InitMariaDB;
import fr.euphyllia.skylliachallenge.storage.InitSQLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkylliaChallenge extends JavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(SkylliaChallenge.class);
    private static SkylliaChallenge instance;
    private ChallengeManagers challengeManager;


    public static SkylliaChallenge getInstance() {
        return instance;
    }

    public ChallengeManagers getChallengeManager() {
        return challengeManager;
    }

    @Override
    public void onEnable() {
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

        this.challengeManager = new ChallengeManagers(this);
        this.challengeManager.loadChallenges(getDataFolder().toPath().resolve("challenges").toFile());

        SkylliaAPI.registerCommands(new ChallengeCommand(this), "challenge");
        SkylliaAPI.registerAdminCommands(new ChallengeCommand(this), "challenge");
    }

    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
    }
}
