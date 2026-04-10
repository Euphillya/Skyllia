package fr.euphyllia.skylliapanel.configuration;

import fr.euphyllia.skylliapanel.gui.dynamic.DynamicGuiType;
import fr.euphyllia.skylliapanel.gui.dynamic.DynamicItemDefinition;

import java.util.List;

public record GuiDefinition(
        String fileName,
        String title,
        int rows,
        List<ButtonDefinition> buttons,
        DynamicGuiType dynamicType,
        List<Integer> dynamicSlots,
        DynamicItemDefinition dynamicItem
) {
    public GuiDefinition {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must not be null or blank");
        }
        if (title == null) {
            title = "<dark_gray>Panel";
        }
        if (rows < 1) {
            rows = 1;
        }
        if (rows > 6) {
            rows = 6;
        }
        if (buttons == null) {
            buttons = List.of();
        }
        if (dynamicSlots == null) {
            dynamicSlots = List.of();
        }
    }

    public boolean isDynamic() {
        return dynamicType != null;
    }

    public int pageSize() {
        return dynamicSlots.isEmpty() ? 0 : dynamicSlots.size();
    }
}
