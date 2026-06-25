package fr.euphyllia.skylliachallenge.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.challenge.ChallengeLevel;
import fr.euphyllia.skylliachallenge.managers.ChallengeLevelManagers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI listing the configured {@link ChallengeLevel} tiers and their progression for the player's island.
 */
public class ChallengeLevelGui {

    private static final Logger log = LoggerFactory.getLogger(ChallengeLevelGui.class);
    private final SkylliaChallenge plugin;
    private final ChallengeLevelManagers manager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private int currentPage = 1;

    public ChallengeLevelGui(SkylliaChallenge plugin, ChallengeLevelManagers manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player, int page) {
        this.currentPage = page;
        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "addons.challenge.player.no-island");
            return;
        }

        manager.evaluate(island, player);

        GuiSettings gs = plugin.getGuiSettings();

        Gui gui = Gui.gui()
                .title(ConfigLoader.language.translate(player.locale(), "addons.challenge.level.display.title", Map.of(), false))
                .rows(gs.rows)
                .disableAllInteractions()
                .create();

        gui.setItem(gs.previous.row(), gs.previous.column(),
                ItemBuilder.from(gs.previous.toItemStack())
                        .name(ConfigLoader.language.translate(player.locale(), "addons.challenge.display.previous", Map.of(), false))
                        .asGuiItem(e -> {
                            final int previousPage = currentPage - 1;
                            Bukkit.getAsyncScheduler().runNow(plugin, task -> {
                                if (previousPage > 0) {
                                    open(player, previousPage);
                                } else {
                                    open(player, gs.maxPageSize);
                                }
                            });
                        }));

        gui.setItem(gs.next.row(), gs.next.column(),
                ItemBuilder.from(gs.next.toItemStack())
                        .name(ConfigLoader.language.translate(player.locale(), "addons.challenge.display.next", Map.of(), false))
                        .asGuiItem(e -> {
                            final int nextPage = currentPage + 1;
                            Bukkit.getAsyncScheduler().runNow(plugin, task -> {
                                if (nextPage <= gs.maxPageSize) {
                                    open(player, nextPage);
                                } else {
                                    open(player, 1);
                                }
                            });
                        }));

        for (ChallengeLevel level : manager.getLevels()) {
            if (!level.isShowInGUI()) continue;
            Challenge.PositionGUI pos = level.getPositionGUI();
            if (pos == null) continue;
            if (pos.page() != currentPage) continue;
            if (pos.row() <= 0 || pos.row() > gs.rows || pos.column() <= 0 || pos.column() > 9) {
                log.warn("Invalid GUI position for challenge level {}", level.getId());
                continue;
            }

            boolean unlocked = manager.isUnlocked(island, level);
            boolean completed = manager.isCompleted(island, level);
            int done = manager.countCompletedChallenges(island, level);
            int required = manager.getRequiredCount(level);

            ItemStack base = (!unlocked && level.getLockedItem() != null)
                    ? level.getLockedItem().clone()
                    : level.getGuiItem().clone();

            List<Component> lore = new ArrayList<>();
            if (level.getLore() != null) lore.addAll(level.getLore());
            lore.add(miniMessage.deserialize("<gray>--------------------</gray>"));

            lore.add(ConfigLoader.language.translate(player.locale(), "addons.challenge.level.display.progression", Map.of(
                    "%done%", String.valueOf(done),
                    "%required%", String.valueOf(required),
                    "%total%", String.valueOf(level.getChallenges() == null ? 0 : level.getChallenges().size())
            ), false));

            if (level.getMaxSkip() > 0) {
                lore.add(ConfigLoader.language.translate(player.locale(), "addons.challenge.level.display.max-skip", Map.of(
                        "%max_skip%", String.valueOf(level.getMaxSkip())
                ), false));
            }

            String stateKey = completed
                    ? "addons.challenge.level.display.completed"
                    : (unlocked ? "addons.challenge.level.display.unlocked" : "addons.challenge.level.display.locked");
            lore.add(ConfigLoader.language.translate(player.locale(), stateKey, Map.of(), false));

            if (level.getGuiLore() != null) lore.addAll(level.getGuiLore());

            gui.setItem(pos.row(), pos.column(),
                    ItemBuilder.from(base).lore(lore).name(miniMessage.deserialize(level.getName()))
                            .asGuiItem(e -> {
                            }));
        }

        player.getScheduler().run(plugin, task -> gui.open(player), null);
    }
}
