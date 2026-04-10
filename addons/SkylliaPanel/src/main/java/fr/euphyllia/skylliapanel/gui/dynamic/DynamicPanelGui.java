package fr.euphyllia.skylliapanel.gui.dynamic;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skylliapanel.SkylliaPanelPlugin;
import fr.euphyllia.skylliapanel.action.ActionDefinition;
import fr.euphyllia.skylliapanel.configuration.ButtonDefinition;
import fr.euphyllia.skylliapanel.configuration.GuiDefinition;
import fr.euphyllia.skylliapanel.util.ItemFactory;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DynamicPanelGui {

    private static final Logger log = LoggerFactory.getLogger(DynamicPanelGui.class);
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final GuiDefinition definition;
    private final int page;

    public DynamicPanelGui(GuiDefinition definition, int page) {
        this.definition = definition;
        this.page = Math.max(1, page);
    }

    public void open(Player viewer) {
        Island island = SkylliaAPI.getIslandByPlayerId(viewer.getUniqueId());
        if (island == null) {
            // todo message pas d'ile
            return;
        }

        List<DynamicContext> entries = collectEntries(island);
        int pageSize = definition.pageSize();
        int totalPages = pageSize > 0 ? Math.max(1, (int) Math.ceil((double) entries.size() / pageSize)) : 1;
        int currentPage = Math.min(page, totalPages);

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, entries.size());
        List<DynamicContext> pageEntries = (pageSize > 0 && fromIndex < entries.size())
                ? entries.subList(fromIndex, toIndex)
                : List.of();

        String resolvedTitle = PlaceholderAPI.setPlaceholders(viewer, definition.title())
                .replace("%page%", String.valueOf(currentPage))
                .replace("%total_pages%", String.valueOf(totalPages))
                .replace("%total_entries%", String.valueOf(entries.size()));

        Gui gui = Gui.gui()
                .title(miniMessage.deserialize(resolvedTitle))
                .rows(definition.rows())
                .disableAllInteractions()
                .create();

        for (ButtonDefinition btn : definition.buttons()) {
            try {
                ButtonDefinition resolved = resolvePageVariables(btn, currentPage, totalPages, entries.size());
                GuiItem item = ItemFactory.buildGuiItem(viewer, resolved);
                gui.setItem(resolved.slot(), item);
            } catch (Exception e) {
                log.error("Error while resolving page", e);
            }
        }

        List<Integer> slots = definition.dynamicSlots();
        DynamicItemDefinition itemDef = definition.dynamicItem();

        if (itemDef != null && !slots.isEmpty()) {
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);
                if (i < pageEntries.size()) {
                    DynamicContext ctx = pageEntries.get(i);
                    GuiItem item = DynamicItemFactory.build(viewer, itemDef, ctx);
                    gui.setItem(slot, item);
                }
            }
        }

        viewer.getScheduler().run(
                SkylliaPanelPlugin.getInstance(),
                task -> gui.open(viewer),
                null
        );
    }

    @NotNull
    private List<DynamicContext> collectEntries(Island island) {
        return switch (definition.dynamicType()) {
            case MEMBERS -> {
                List<Players> members = island.getMembers();
                List<DynamicContext> list = new ArrayList<>();
                if (members != null) {
                    for (Players p : members) {
                        list.add(new DynamicContext(p, null));
                    }
                }
                yield list;
            }
            case BANNED -> {
                List<Players> banned = island.getBannedMembers();
                List<DynamicContext> list = new ArrayList<>();
                if (banned != null) {
                    for (Players p : banned) {
                        list.add(new DynamicContext(p, null));
                    }
                }
                yield list;
            }
            case WARPS -> {
                List<WarpIsland> warps = island.getWarps();
                List<DynamicContext> list = new ArrayList<>();
                if (warps != null) {
                    for (WarpIsland w : warps) {
                        list.add(new DynamicContext(null, w));
                    }
                }
                yield list;
            }
            case PERMISSIONS -> new ArrayList<>(); // Todo : add permission later
        };
    }

    private ButtonDefinition resolvePageVariables(ButtonDefinition btn, int currentPage, int totalPages, int totalEntries) {
        int prevPage = currentPage > 1 ? currentPage - 1 : totalPages;
        int nextPage = currentPage < totalPages ? currentPage + 1 : 1;

        var resolvedActions = btn.actions().stream()
                .map(action -> new ActionDefinition(
                        action.type(),
                        action.value()
                                .replace("%page%", String.valueOf(currentPage))
                                .replace("%prev_page%", String.valueOf(prevPage))
                                .replace("%next_page%", String.valueOf(nextPage))
                                .replace("%total_pages%", String.valueOf(totalPages))
                                .replace("%total_entries%", String.valueOf(totalEntries))
                ))
                .toList();

        String resolvedName = btn.name()
                .replace("%page%", String.valueOf(currentPage))
                .replace("%prev_page%", String.valueOf(prevPage))
                .replace("%next_page%", String.valueOf(nextPage))
                .replace("%total_pages%", String.valueOf(totalPages))
                .replace("%total_entries%", String.valueOf(totalEntries));

        List<String> resolvedLore = btn.lore().stream()
                .map(line -> line
                        .replace("%page%", String.valueOf(currentPage))
                        .replace("%prev_page%", String.valueOf(prevPage))
                        .replace("%next_page%", String.valueOf(nextPage))
                        .replace("%total_pages%", String.valueOf(totalPages))
                        .replace("%total_entries%", String.valueOf(totalEntries)))
                .toList();

        return new ButtonDefinition(
                btn.slot(), btn.material(), btn.skullTexture(), btn.skullPlayer(),
                resolvedName, resolvedLore, btn.glow(), btn.customModelData(), btn.itemModel(), resolvedActions
        );
    }
}
