package fr.euphyllia.skyllia.api.addons;

import org.bukkit.plugin.Plugin;

public interface SkylliaAddon {

    /**
     * Indique la phase de chargement dans laquelle l'addon souhaite être initialisé.
     */
    AddonLoadPhase getLoadPhase();

    /**
     * Chargé quand l'addon est détecté, donc selon la phase (BEFORE/AFTER)
     */
    void onLoad(Plugin plugin);

    /**
     * Appelé juste après onLoad (dans la même phase).
     * Utile si tu veux séparer la configuration/initialisation (onLoad)
     * de l'activation réelle (onEnable).
     */
    void onEnable();

    /**
     * Appelé quand on désactive l'addon.
     */
    void onDisabled();
}
