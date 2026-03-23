package fr.euphyllia.skylliaacidrain.listener;

import fr.euphyllia.skylliaacidrain.SkylliaAcidRain;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Listener;

public class AcidListener implements Listener {

    private final SkylliaAcidRain plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AcidListener(SkylliaAcidRain plugin) {
        this.plugin = plugin;
    }
}
