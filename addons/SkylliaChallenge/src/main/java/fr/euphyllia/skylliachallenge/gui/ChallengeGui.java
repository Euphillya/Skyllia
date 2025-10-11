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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChallengeGui {

    private static final DecimalFormat NF = new DecimalFormat("#,###");
    private final SkylliaChallenge plugin;
    private final ChallengeManagers manager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChallengeGui(SkylliaChallenge plugin, ChallengeManagers manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player) {
        Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "addons.challenge.player.no-island");
            return;
        }

        Gui gui = Gui.gui().title(MiniMessage.miniMessage().deserialize("<green>Challenges d'île</green>")).rows(6).create();
        gui.disableAllInteractions();

        for (Challenge c : manager.getChallenges()) {
            if (!c.isShowInGUI()) continue;

            int times = ProgressStorage.getTimesCompleted(island.getId(), c.getId());
            boolean can = manager.canComplete(island, c, player);

            ItemStack base = c.getGuiItem().clone();
            List<Component> lore = new ArrayList<>();
            lore.addAll(c.getLore());
            lore.add(miniMessage.deserialize("<gray>--------------------</gray>"));
            lore.add(ConfigLoader.language.translate(player, "addons.challenge.display.progression",
                    Map.of(
                            "progression", String.valueOf(times),
                            "goal", c.getMaxTimes() >= 0 ? String.valueOf(c.getMaxTimes()) : "∞"
                    )));

            if (c.getRequirements() != null && !c.getRequirements().isEmpty()) {
                lore.add(ConfigLoader.language.translate(player, "addons.challenge.display.requirements"));
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
            lore.add(can ? ConfigLoader.language.translate(player, "addons.challenge.display.can_validate") : ConfigLoader.language.translate(player, "addons.challenge.display.cannot_validate"));
            if (c.getGuiLore() != null) lore.addAll(c.getGuiLore());

            gui.setItem(c.getSlot(), ItemBuilder.from(base).lore(lore).asGuiItem(e -> {
                if (manager.complete(island, c, player)) {
                    ConfigLoader.language.translate(player, "addons.challenge.player.complete", Map.of(
                            "challenge_name", c.getName()
                    ));
                }
                Bukkit.getAsyncScheduler().runNow(plugin, task -> open(player));
            }));
        }

        player.getScheduler().run(plugin, task -> gui.open(player), null);
    }
}