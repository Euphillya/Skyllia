package fr.euphyllia.skylliachallenge;

import dev.triumphteam.gui.TriumphGui;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliachallenge.commands.ChallengeAdminCommand;
import fr.euphyllia.skylliachallenge.commands.ChallengeCommand;
import fr.euphyllia.skylliachallenge.gui.GuiSettings;
import fr.euphyllia.skylliachallenge.listener.*;
import fr.euphyllia.skylliachallenge.managers.ChallengeManagers;
import fr.euphyllia.skylliachallenge.storage.InitMariaDB;
import fr.euphyllia.skylliachallenge.storage.InitSQLite;
import fr.euphyllia.skylliachallenge.storage.ProgressStorage;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SkylliaChallenge extends JavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(SkylliaChallenge.class);
    private static SkylliaChallenge instance;
    private ChallengeManagers challengeManager;
    private GuiSettings guiSettings;
    private boolean mustBeOnPlayerIsland;

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

        // ─────────────────────────────────────────────
        String violet = "§d";
        String gray = "§7";
        String white = "§f";
        String separator = violet + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        List<String> logs = new ArrayList<>();
        logs.add(separator);
        logs.add(violet + " » SkylliaChallenge is starting...");

        // Load Progress Storage Threads
        int psThreads = getConfig().getInt("progress_storage.threads", 2);
        ProgressStorage.initExecutor(psThreads);
        logs.add(gray + " » " + white + "ProgressStorage Threads: " + violet + psThreads);

        int pspThreads = getConfig().getInt("partial_progress_storage.threads", 2);
        ProgressStoragePartial.initExecutor(pspThreads);
        logs.add(gray + " » " + white + "PartialProgressStorage Threads: " + violet + pspThreads);

        mustBeOnPlayerIsland = getConfig().getBoolean("must_be_on_player_island", true);

        // ─────────────────────────────────────────────
        getDataFolder().mkdirs();
        getDataFolder().toPath().resolve("challenges").toFile().mkdirs();

        boolean usingMaria;
        try {
            usingMaria = InitMariaDB.initIfConfigured();
            if (!usingMaria) InitSQLite.initIfConfigured();
        } catch (Exception exception) {
            log.error("Error during database initialization: {}", exception.getMessage(), exception);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        logs.add(gray + " » " + white + "Database: " + (usingMaria ? violet + "MariaDB" : violet + "SQLite"));

        // ─────────────────────────────────────────────
        this.guiSettings = GuiSettings.load(getConfig());
        this.challengeManager = new ChallengeManagers(this);
        this.challengeManager.loadChallenges(getDataFolder().toPath().resolve("challenges").toFile());
        logs.add(gray + " » " + white + "Challenges Loaded: " + violet + challengeManager.getChallenges().size());

        // Commands
        SkylliaAPI.registerCommands(new ChallengeCommand(this), "challenge");
        SkylliaAPI.registerAdminCommands(new ChallengeAdminCommand(this), "challenge");

        ProgressStorage.preloadAllProgress();
        ProgressStoragePartial.preloadAllPartialProgress();

        Bukkit.getAsyncScheduler().runAtFixedRate(
                this,
                task -> ProgressStoragePartial.flushDirty(),
                1, 1, TimeUnit.MINUTES
        );

        logs.add(separator);
        Bukkit.getConsoleSender().sendMessage(logs.toArray(new String[0]));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CraftRequirementListener(), this);
        pm.registerEvents(new BlockRequirementListener(), this);
        pm.registerEvents(new KillRequirementListener(), this);
        pm.registerEvents(new PlayerEnchantRequirementListener(), this);
        pm.registerEvents(new PlayerConsumeRequirementListener(), this);
        pm.registerEvents(new PlayerFishRequirementListener(), this);
    }


    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        ProgressStoragePartial.shutdown();
        ProgressStorage.shutdown();
    }

    public void reload() {
        this.guiSettings = GuiSettings.load(getConfig());
        this.mustBeOnPlayerIsland = getConfig().getBoolean("must_be_on_player_island", true);
        getChallengeManager().loadChallenges(getDataFolder().toPath().resolve("challenges").toFile());
    }

    public boolean isMustBeOnPlayerIsland() {
        return mustBeOnPlayerIsland;
    }
}
