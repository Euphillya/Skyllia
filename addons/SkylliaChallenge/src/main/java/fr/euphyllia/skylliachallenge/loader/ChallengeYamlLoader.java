package fr.euphyllia.skylliachallenge.loader;

import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.*;
import fr.euphyllia.skylliachallenge.reward.BankReward;
import fr.euphyllia.skylliachallenge.reward.CommandReward;
import fr.euphyllia.skylliachallenge.reward.ItemReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChallengeYamlLoader {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final boolean hasSkylliaBank = Bukkit.getPluginManager().getPlugin("SkylliaBank") != null;
    private static final boolean hasVault = Bukkit.getPluginManager().getPlugin("Vault") != null;
    private static final Logger log = LoggerFactory.getLogger(ChallengeYamlLoader.class);

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
        List<Component> lore = yml.getStringList("lore").stream().map(miniMessage::deserialize).toList();
        List<Component> itemLore = yml.getStringList("itemLore").stream().map(miniMessage::deserialize).toList();
        String cooldownStr = yml.getString("cooldown", null);

        Challenge challenge = new Challenge(id)
                .setName(yml.getString("name", idStr))
                .setLore(lore)
                .setMaxTimes(yml.getInt("maxTimes", -1))
                .setBroadcastCompletion(yml.getBoolean("broadcast", false))
                .setShowInGUI(yml.getBoolean("showInGui", true))
                .setPositionGUI(parsePositionGUI(yml))
                .setGuiItemAmount(yml.getInt("amount", 1))
                .setGuiLore(itemLore);

        if (cooldownStr != null) {
            challenge.setCooldownMillis(parseDurationToMillis(cooldownStr));
        }

        String guiMat = yml.getString("item", "STONE");
        Material mat = Material.matchMaterial(guiMat);
        if (mat == null) mat = Material.STONE;
        challenge.setGuiItem(new ItemStack(mat, Math.max(1, challenge.getGuiItemAmount())));

        // REQUIREMENTS
        List<String> reqRaw = yml.getStringList("requirements");
        List<ChallengeRequirement> req = parseRequirements(plugin, id, reqRaw);
        challenge.setRequirements(req);

        // REWARDS
        List<String> rewRaw = yml.getStringList("rewards");
        List<ChallengeReward> rew = parseRewards(rewRaw);
        challenge.setRewards(rew);

        return challenge;
    }

    private static List<ChallengeRequirement> parseRequirements(SkylliaChallenge plugin, NamespacedKey challengeKey, List<String> lines) {
        if (lines == null) return List.of();
        List<ChallengeRequirement> result = new ArrayList<>();
        for (int idx = 0; idx < lines.size(); idx++) {
            String line = lines.get(idx);
            String[] sp = line.trim().split("\\s+");
            if (sp.length == 0) continue;
            String head = sp[0];
            if (head.startsWith("ITEM:")) {
                try {
                    Material material = Material.matchMaterial(head.substring("ITEM:".length()));
                    if (material == null) continue;
                    int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    String itemName = material.name(); // default

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
                } catch (Exception e) {
                    log.error("Invalid material for ITEM requirement in challenge {}: {}", challengeKey, head.substring("ITEM:".length()), e);
                }
            }
            if (head.startsWith("CRAFT:")) {
                try {
                    Material material = Material.matchMaterial(head.substring("CRAFT:".length()));
                    if (material == null) continue;
                    int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    String itemName = material.name();
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
                    result.add(new CraftRequirement(idx, challengeKey, material, count, itemName, customModelData, itemModel));
                } catch (Exception e) {
                    log.error("Invalid material for CRAFT requirement in challenge {}: {}", challengeKey, head.substring("CRAFT:".length()), e);
                }
            }
            if (head.startsWith("NEAR:")) {
                try {
                    EntityType t = EntityType.valueOf(head.substring("NEAR:".length()).toUpperCase(Locale.ROOT));
                    int amount = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    double radius = sp.length > 2 ? Double.parseDouble(sp[2]) : 8.0D;
                    result.add(new NearEntityRequirement(t, amount, radius));
                } catch (Exception e) {
                    log.error("Invalid entity type for NEAR requirement in challenge {}: {}", challengeKey, head.substring("NEAR:".length()), e);
                }
            }
            if (head.startsWith("POTION:")) {
                try {
                    PotionType p = PotionType.valueOf(sp[1].toUpperCase(Locale.ROOT));
                    int data = Integer.parseInt(sp[2]);
                    int amount = Integer.parseInt(sp[3]);
                    result.add(new PotionRequirement(idx, challengeKey, p, data, amount));
                } catch (IllegalArgumentException e) {
                    log.error("Invalid potion type for POTION requirement in challenge {}: {}", challengeKey, sp[1], e);
                }
            }
            if (head.startsWith("BLOCKBREAK:")) {
                try {
                    Material material = Material.matchMaterial(head.substring("BLOCKBREAK:".length()));
                    if (material == null) continue;
                    int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    result.add(new BlockBreakRequirement(idx, challengeKey, material, count, material.name()));
                } catch (IllegalArgumentException exception) {
                    log.error("Invalid material for BLOCKBREAK requirement in challenge {}: {}", challengeKey, head.substring("BLOCKBREAK:".length()), exception);
                }
            }
            if (head.startsWith("ENCHANTMENT:")) {
                try {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(head.substring("ENCHANTMENT:".length())));
                    int level = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    int count = sp.length > 2 ? Integer.parseInt(sp[2]) : 1;
                    boolean strict = sp.length > 3 && Boolean.parseBoolean(sp[3]);
                    result.add(new EnchantRequirement(idx, challengeKey, enchantment, level, count, strict));
                } catch (Exception e) {
                    log.error("Invalid enchantment for ENCHANTMENT requirement in challenge {}: {}", challengeKey, head.substring("ENCHANTMENT:".length()), e);
                }
            }
            if (head.startsWith("FISH:")) {
                try {
                    Material entityType = Material.valueOf(head.substring("FISH:".length()).toUpperCase(Locale.ROOT));
                    int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    result.add(new FishRequirement(idx, challengeKey, entityType, count));
                } catch (IllegalArgumentException exception) {
                    log.error("Invalid material for FISH requirement in challenge {}: {}", challengeKey, head.substring("FISH:".length()), exception);
                }
            }
            if (head.startsWith("KILL:")) {
                try {
                    EntityType entityType = EntityType.valueOf(head.substring("KILL:".length()).toUpperCase(Locale.ROOT));
                    int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                    result.add(new KillEntityRequirement(idx, challengeKey, entityType, count));
                } catch (IllegalArgumentException exception) {
                    log.error("Invalid entity type for KILL requirement in challenge {}: {}", challengeKey, head.substring("KILL:".length()), exception);
                }
            }
            if (head.startsWith("CONSUME:")) {
                String materialRaw = head.substring("CONSUME:".length());
                int count = sp.length > 1 ? Integer.parseInt(sp[1]) : 1;
                result.add(new PlayerConsumeRequirement(idx, challengeKey, materialRaw, count));
            }
            if (hasSkylliaBank) {
                if (head.startsWith("BANK:")) {
                    try {
                        double amount = Double.parseDouble(head.substring("BANK:".length()));
                        result.add(new BankRequirement(idx, challengeKey, amount));
                    } catch (NumberFormatException exception) {
                        log.error("Invalid amount for BANK requirement in challenge {}: {}", challengeKey, head.substring("BANK:".length()), exception);
                    }
                }
            }
            if (hasVault) {
                if (head.startsWith("ECO:")) {
                    try {
                        double amount = Double.parseDouble(head.substring("ECO:".length()));
                        result.add(new EcoRequirement(idx, challengeKey, amount));
                    } catch (NumberFormatException exception) {
                        log.error("Invalid amount for ECO requirement in challenge {}: {}", challengeKey, head.substring("ECO:".length()), exception);
                    }
                }
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
                } else if (head.startsWith("BANK:") && hasSkylliaBank) {
                    double amount = Double.parseDouble(head.substring("BANK:".length()).trim());
                    list.add(new BankReward(amount));
                }
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    private static Challenge.PositionGUI parsePositionGUI(YamlConfiguration yml) {
        int page = yml.getInt("gui.page", 1);  // par défaut page 1
        int row = yml.getInt("gui.row", 1);    // par défaut row 1
        int column = yml.getInt("gui.column", 1); // default column 1
        return new Challenge.PositionGUI(page, row, column);
    }

    private static long parseDurationToMillis(String input) {
        long total = 0L;

        Matcher matcher = Pattern.compile("(\\d+)(ms|s|m|min|h|d|day|w|week|month|y|year)").matcher(input.toLowerCase());

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "ms" -> total += value;
                case "s" -> total += value * 1000L;
                case "m", "min" -> total += value * 60_000L;
                case "h" -> total += value * 3_600_000L;
                case "d", "day" -> total += value * 86_400_000L;
                case "w", "week" -> total += value * 7 * 86_400_000L;
                case "month" -> total += value * 30L * 86_400_000L;
                case "y", "year" -> total += value * 365L * 86_400_000L;
            }
        }

        return total;
    }

}