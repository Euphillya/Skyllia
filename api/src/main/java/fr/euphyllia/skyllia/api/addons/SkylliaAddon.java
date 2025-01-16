package fr.euphyllia.skyllia.api.addons;

import org.bukkit.plugin.Plugin;

public interface SkylliaAddon {

    void onLoad(Plugin plugin);

    void onEnable();

    void onDisabled();
}
