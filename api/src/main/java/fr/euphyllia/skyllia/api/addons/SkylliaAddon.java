package fr.euphyllia.skyllia.api.addons;

import org.bukkit.plugin.Plugin;

public interface SkylliaAddon {

    /**
     * Indicates the loading phase in which the addon wishes to be initialized.
     */
    AddonLoadPhase getLoadPhase();

    /**
     * Called when the addon is detected, according to its loading phase (BEFORE/AFTER).
     */
    void onLoad(Plugin plugin);

    /**
     * Called immediately after onLoad (within the same phase).
     * Useful if you want to separate configuration/initialization (onLoad)
     * from the actual activation (onEnable).
     */
    void onEnable();

    /**
     * Called when the addon is deactivated.
     */
    void onDisabled();
}
