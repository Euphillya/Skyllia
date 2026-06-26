package fr.euphyllia.skyllia.listeners.extra;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.IslandInfoEvent;
import fr.euphyllia.skyllia.api.utils.Keys;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class IslandInfoExtraListener implements Listener {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @EventHandler
    public void onIslandInfoEvent(final IslandInfoEvent event) {
        String rawName = SkylliaAPI.getIslandCustomDataQuery().get(
                Keys.NAMESPACE_KEY_EXTRA,
                event.getIsland(),
                Keys.KEY_NAME,
                PersistentDataType.STRING
        );

        String rawDescription = SkylliaAPI.getIslandCustomDataQuery().get(
                Keys.NAMESPACE_KEY_EXTRA,
                event.getIsland(),
                Keys.KEY_DESCRIPTION,
                PersistentDataType.STRING
        );

        if (rawName == null && rawDescription == null) {
            return;
        }

        var builder = Component.text().append(
                ConfigLoader.language.translate(event.getViewer(), "addons.skylliaextra.display.title")
        );

        if (rawName != null) {
            builder.append(Component.newline())
                    .append(ConfigLoader.language.translate(
                            event.getViewer(),
                            "addons.skylliaextra.display.name",
                            Map.of("%name%", rawName)
                    ));
        }

        if (rawDescription != null) {
            builder.append(Component.newline())
                    .append(ConfigLoader.language.translate(
                            event.getViewer(),
                            "addons.skylliaextra.display.description",
                            Map.of("%description%", rawDescription)
                    ));
        }

        event.addLine(builder.build());
    }
}
