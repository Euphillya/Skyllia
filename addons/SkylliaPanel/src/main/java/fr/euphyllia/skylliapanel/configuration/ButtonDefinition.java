package fr.euphyllia.skylliapanel.configuration;

import fr.euphyllia.skylliapanel.action.ActionDefinition;

import java.util.List;

public record ButtonDefinition(
        int slot,
        String material,
        String skullTexture,
        String skullPlayer,
        String name,
        List<String> lore,
        boolean glow,
        int customModelData, // -1.21.4
        String itemModel, // 1.21.4+
        List<ActionDefinition> actions
) {
}
