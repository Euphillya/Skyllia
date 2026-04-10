package fr.euphyllia.skylliapanel.gui.dynamic;

import fr.euphyllia.skylliapanel.action.ActionDefinition;

import java.util.ArrayList;
import java.util.List;

public record DynamicItemDefinition(
        String material,
        String name,
        List<String> lore,
        boolean glow,
        int customModelData, // -1.21.4
        String itemModel, // 1.21.4+
        List<ActionDefinition> leftClickActions,
        List<ActionDefinition> rightClickActions
) {
    public DynamicItemDefinition {
        if (material == null || material.isBlank()) {
            material = "STONE";
        }
        if (name == null || name.isBlank()) {
            name = material;
        }

        if (lore == null) {
            lore = new ArrayList<>();
        }
        if (leftClickActions == null) {
            leftClickActions = new ArrayList<>();
        }
        if (rightClickActions == null) {
            rightClickActions = new ArrayList<>();
        }
    }
}
