package fr.euphyllia.skylliachallenge.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.managers.ChallengeManagers;
import fr.euphyllia.skylliachallenge.requirement.ItemRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStorage;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChallengeGui {

    private static final DecimalFormat NF = new DecimalFormat("#,###");
    private static final Logger log = LoggerFactory.getLogger(ChallengeGui.class);
    private final SkylliaChallenge plugin;
    private final ChallengeManagers manager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private int currentPage = 1;

    public ChallengeGui(SkylliaChallenge plugin, ChallengeManagers manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player, int page) {
        this.currentPage = page;
        Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "addons.challenge.player.no-island");
            return;
        }

        GuiSettings gs = plugin.getGuiSettings();

        Gui gui = Gui.gui()
                .title(ConfigLoader.language.translate(player.locale(), "addons.challenge.display.title", Map.of(), false))
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

        // Navigation: Next
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

        for (Challenge c : manager.getChallenges()) {
            if (!c.isShowInGUI()) continue;
            Challenge.PositionGUI pos = c.getPositionGUI();
            if (pos == null) continue;
            if (pos.page() != currentPage) continue;
            if (pos.row() <= 0 || pos.row() > gs.rows || pos.column() <= 0 || pos.column() > 9) {
                log.warn("Invalid GUI position for challenge {}", c.getId());
                continue;
            }


            int times = ProgressStorage.getTimesCompleted(island.getId(), c.getId());
            boolean can = manager.canComplete(island, c, player);

            ItemStack base = c.getGuiItem().clone();
            List<Component> lore = new ArrayList<>();
            lore.addAll(c.getLore());
            lore.add(miniMessage.deserialize("<gray>--------------------</gray>"));
            lore.add(ConfigLoader.language.translate(player.locale(), "addons.challenge.display.progression",
                    Map.of(
                            "%progression%", String.valueOf(times),
                            "%max_times%", c.getMaxTimes() >= 0 ? String.valueOf(c.getMaxTimes()) : "∞"
                    ), false));

            if (c.getRequirements() != null && !c.getRequirements().isEmpty()) {
                lore.add(ConfigLoader.language.translate(player.locale(), "addons.challenge.display.requirements", Map.of(), false));
                for (ChallengeRequirement req : c.getRequirements()) {
                    if (req instanceof ItemRequirement ir) {
                        long collected = ProgressStoragePartial.getPartial(island.getId(), c.getId(), ir.requirementId());
                        long target = ir.count();
                        boolean met = collected >= target;
                        String mat = ir.getDisplay();
                        lore.add(miniMessage.deserialize(
                                (met ? "<green> • " : "<gray> • ")
                                        + mat + ": <white>" + NF.format(collected) + "</white>/<white>" + NF.format(target) + "</white>"
                                        + (met ? " ✓" : "")
                        ));
                    } else {
                        boolean met = req.isMet(player, island);
                        lore.add(miniMessage.deserialize(
                                (met ? "<green> • " : "<gray> • ") + req.getDisplay() + (met ? " ✓" : "")
                        ));
                    }
                }
            }
            lore.add(can ? ConfigLoader.language.translate(player.locale(), "addons.challenge.display.can-validate", Map.of(), false) :
                    ConfigLoader.language.translate(player.locale(), "addons.challenge.display.cannot-validate", Map.of(), false));
            if (c.getGuiLore() != null) lore.addAll(c.getGuiLore());

            gui.setItem(pos.row(), pos.column(), ItemBuilder.from(base).lore(lore).asGuiItem(e -> {
                if (manager.complete(island, c, player)) {
                    ConfigLoader.language.sendMessage(player, "addons.challenge.player.complete", Map.of(
                            "%challenge_name%", c.getName()
                    ));
                }
                Bukkit.getAsyncScheduler().runNow(plugin, task -> open(player, currentPage));
            }));
        }

        player.getScheduler().run(plugin, task -> gui.open(player), null);
    }

}