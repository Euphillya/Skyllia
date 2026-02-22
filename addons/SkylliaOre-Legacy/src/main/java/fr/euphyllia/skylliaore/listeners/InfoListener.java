package fr.euphyllia.skylliaore.listeners;

import fr.euphyllia.skyllia.api.event.IslandInfoEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliaore.SkylliaOre;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class InfoListener implements Listener {

    @EventHandler
    public void onIslandInfoEvent(final IslandInfoEvent event) {
        Island island = event.getIsland();
        var gen = SkylliaOre.getCachedGenerator(island.getId());
        if (gen == null) return;

        Component component = Component.text("")
                .append(ConfigLoader.language.translate(event.getViewer(), "addons.ore.display.title"))
                .append(Component.newline())
                .append(ConfigLoader.language.translate(event.getViewer(), "addons.ore.display.generator", Map.of(
                        "%ore%", gen.name()
                )));

        event.addLine(component);
    }
}
