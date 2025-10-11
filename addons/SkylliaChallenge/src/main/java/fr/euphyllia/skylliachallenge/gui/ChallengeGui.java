package fr.euphyllia.skylliachallenge.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.managers.ChallengeManagers;
import fr.euphyllia.skylliachallenge.storage.ProgressStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChallengeGui {

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
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Vous n'avez pas d'île.</red>"));
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
            lore.add(miniMessage.deserialize("<gray>Progression: <white>" + times + (c.getMaxTimes() >= 0 ? ("/" + c.getMaxTimes()) : "/∞") + "</white></gray>"));
            lore.add(miniMessage.deserialize(can ? "<green>Clique pour valider</green>" : "<red>Conditions non remplies</red>"));
            if (c.getGuiLore() != null) lore.addAll(c.getGuiLore());

            gui.setItem(c.getSlot(), ItemBuilder.from(base).lore(lore).asGuiItem(e -> {
                if (manager.complete(island, c, player)) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Défi complété:</green> <yellow>" + c.getName() + "</yellow>"));
                    open(player);
                } else {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Vous ne remplissez pas les conditions.</red>"));
                }
            }));
        }

        player.getScheduler().run(plugin, task -> gui.open(player), null);
    }
}