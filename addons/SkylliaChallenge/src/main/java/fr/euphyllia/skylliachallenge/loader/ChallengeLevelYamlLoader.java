package fr.euphyllia.skylliachallenge.loader;

import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.challenge.ChallengeLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads {@link ChallengeLevel} definitions from YAML files.
 * <p>
 * Each level lives in its own {@code .yml} file inside the {@code levels/} folder and follows the
 * same conventions used by {@link ChallengeYamlLoader} for names, lore, rewards and GUI placement.
 * </p>
 */
public final class ChallengeLevelYamlLoader {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Logger log = LoggerFactory.getLogger(ChallengeLevelYamlLoader.class);

    private ChallengeLevelYamlLoader() {
    }

    public static List<ChallengeLevel> loadFolder(SkylliaChallenge plugin, File folder) {
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return List.of();
        List<ChallengeLevel> list = new ArrayList<>();
        for (File f : files) {
            ChallengeLevel level = load(plugin, f);
            if (level != null) list.add(level);
        }
        return list;
    }

    public static ChallengeLevel load(SkylliaChallenge plugin, File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String idStr = yml.getString("id");
        if (idStr == null) {
            plugin.getLogger().warning("Missing id in challenge level " + file.getName());
            return null;
        }
        NamespacedKey id = NamespacedKey.fromString(idStr);
        if (id == null) {
            plugin.getLogger().warning("Invalid id '" + idStr + "' in challenge level " + file.getName());
            return null;
        }

        List<Component> lore = yml.getStringList("lore").stream().map(miniMessage::deserialize).toList();
        List<Component> itemLore = yml.getStringList("itemLore").stream().map(miniMessage::deserialize).toList();

        List<NamespacedKey> challenges = new ArrayList<>();
        for (String raw : yml.getStringList("challenges")) {
            NamespacedKey key = NamespacedKey.fromString(raw);
            if (key == null) {
                log.warn("Invalid challenge id '{}' in challenge level {}", raw, idStr);
                continue;
            }
            challenges.add(key);
        }

        String nextLevelStr = yml.getString("next-level", null);
        NamespacedKey nextLevel = nextLevelStr != null ? NamespacedKey.fromString(nextLevelStr) : null;

        List<ChallengeReward> rewards = ChallengeYamlLoader.parseRewards(yml.getStringList("rewards"));

        int amount = Math.max(1, yml.getInt("amount", 1));

        ChallengeLevel level = new ChallengeLevel(id)
                .setName(yml.getString("name", idStr))
                .setLore(lore)
                .setChallenges(challenges)
                .setMaxSkip(Math.max(0, yml.getInt("max-skip", 0)))
                .setNextLevel(nextLevel)
                .setRewards(rewards)
                .setBroadcastCompletion(yml.getBoolean("broadcast", false))
                .setShowInGUI(yml.getBoolean("showInGui", true))
                .setGuiItemAmount(amount)
                .setGuiLore(itemLore)
                .setPositionGUI(parsePositionGUI(yml));

        String guiMat = yml.getString("item", "STONE");
        level.setGuiItem(ChallengeYamlLoader.resolveGuiItem(guiMat, amount, idStr));

        String lockedMat = yml.getString("icon-locked", null);
        if (lockedMat != null) {
            level.setLockedItem(ChallengeYamlLoader.resolveGuiItem(lockedMat, amount, idStr));
        } else {
            level.setLockedItem(new ItemStack(Material.BARRIER, amount));
        }

        return level;
    }

    private static Challenge.PositionGUI parsePositionGUI(YamlConfiguration yml) {
        int page = yml.getInt("gui.page", 1);
        int row = yml.getInt("gui.row", 1);
        int column = yml.getInt("gui.column", 1);
        return new Challenge.PositionGUI(page, row, column);
    }
}
