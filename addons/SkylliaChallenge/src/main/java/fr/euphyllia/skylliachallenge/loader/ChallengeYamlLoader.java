package fr.euphyllia.skylliachallenge.loader;

import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.*;
import fr.euphyllia.skylliachallenge.reward.CommandReward;
import fr.euphyllia.skylliachallenge.reward.ItemReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ChallengeYamlLoader {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private ChallengeYamlLoader() {
    }

    public static List<Challenge> loadFolder(SkylliaChallenge plugin, File folder) {
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return List.of();
        List<Challenge> list = new ArrayList<>();
        for (File f : files) {
            Challenge c = load(plugin, f);
            if (c != null) list.add(c);
        }
        return list;
    }

    public static Challenge load(SkylliaChallenge plugin, File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String idStr = yml.getString("id");
        if (idStr == null) {
            plugin.getLogger().warning("Missing id in " + file.getName());
            return null;
        }
        NamespacedKey id = NamespacedKey.fromString(idStr);
        List<Component> lore = yml.getStringList("lore").stream().map(s -> miniMessage.deserialize(s)).toList();
        List<Component> itemLore = yml.getStringList("itemLore").stream().map(s -> miniMessage.deserialize(s)).toList();

        Challenge c = new Challenge(id)
                .setName(yml.getString("name", idStr))
                .setLore(lore)
                .setMaxTimes(yml.getInt("maxTimes", -1))
                .setBroadcastCompletion(yml.getBoolean("broadcast", false))
                .setShowInGUI(yml.getBoolean("showInGui", true))
                .setSlot(yml.getInt("slot", 0))
                .setGuiItemAmount(yml.getInt("amount", 1))
                .setGuiLore(itemLore);

        String guiMat = yml.getString("item", "STONE");
        Material mat = Material.matchMaterial(guiMat);
        if (mat == null) mat = Material.STONE;
        c.setGuiItem(new ItemStack(mat, Math.max(1, c.getGuiItemAmount())));

        // REQUIREMENTS
        List<String> reqRaw = yml.getStringList("requirements");
        List<ChallengeRequirement> req = parseRequirements(plugin, id, reqRaw);
        c.setRequirements(req);

        // REWARDS
        List<String> rewRaw = yml.getStringList("rewards");
        List<ChallengeReward> rew = parseRewards(rewRaw);
        c.setRewards(rew);

        return c;
    }

    private static List<ChallengeRequirement> parseRequirements(SkylliaChallenge plugin, NamespacedKey challengeKey, List<String> lines) {
        if (lines == null) return List.of();
        List<ChallengeRequirement> result = new ArrayList<>();
        for (int idx = 0; idx < lines.size(); idx++) {
            String line = lines.get(idx);
            String[] sp = line.trim().split("\\s+");
            if (sp.length == 0) continue;
            String head = sp[0];
            try {
                if (head.startsWith("ITEM:")) {
                    Material material = Material.matchMaterial(head.substring("ITEM:".length()));
                    if (material == null) continue;
                    int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    String itemName = material.name(); // défault

                    int customModelData = -1;
                    NamespacedKey itemModel = null;

                    if (sp.length >= 4) {
                        // Si le 3e param est un nombre → CustomModelData
                        if (sp[3].matches("\\d+")) {
                            itemName = sp[2];
                            customModelData = Integer.parseInt(sp[3]);
                        }
                        // Si c'est un NamespacedKey → ItemModel
                        else if (sp[3].contains(":")) {
                            itemName = sp[3];
                            itemModel = NamespacedKey.fromString(sp[3]);
                        }
                    }
                    result.add(new ItemRequirement(idx, challengeKey, material, count, itemName, customModelData, itemModel));
                }
                if (head.startsWith("NEAR:")) {
                    EntityType t = EntityType.valueOf(head.substring("NEAR:".length()).toUpperCase(Locale.ROOT));
                    int amount = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    double radius = sp.length > 2 ? Double.parseDouble(sp[2]) : 8.0D;
                    result.add(new NearEntityRequirement(t, amount, radius));
                }
                if (head.startsWith("POTION:")) {
                    PotionType p = PotionType.valueOf(sp[1].toUpperCase(Locale.ROOT));
                    int data = Integer.parseInt(sp[2]);
                    int amount = Integer.parseInt(sp[3]);
                    result.add(new PotionRequirement(p, data, amount));
                }
                if (Bukkit.getPluginManager().getPlugin("SkylliaBank") != null) {
                    if (head.startsWith("BANK:")) {
                        double amount = Double.parseDouble(head.substring("BANK:".length()));
                        result.add(new BankRequirement(idx, challengeKey, amount));
                    }
                }
                if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                    if (head.startsWith("ECO:")) {
                        double amount = Double.parseDouble(head.substring("ECO:".length()));
                        result.add(new EcoRequirement(amount));
                    }
                }
            } catch (Exception e) {
                // ignore: retournera null et filtré
            }
        }
        return result;
    }

    private static List<ChallengeReward> parseRewards(List<String> lines) {
        if (lines == null) return List.of();
        List<ChallengeReward> list = new ArrayList<>();
        for (String line : lines) {
            String[] sp = line.trim().split("\\s+");
            if (sp.length == 0) continue;
            String head = sp[0];
            try {
                if (head.startsWith("ITEM:")) {
                    Material m = Material.matchMaterial(head.substring("ITEM:".length()));
                    int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    list.add(new ItemReward(m, count));
                } else if (head.startsWith("CMD:")) {
                    String cmd = line.substring("CMD:".length()).trim();
                    list.add(new CommandReward(cmd));
                }
            } catch (Exception ignored) {
            }
        }
        return list;
    }
}