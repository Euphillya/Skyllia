package fr.euphyllia.skylliapanel.gui.dynamic;

public enum DynamicGuiType {
    MEMBERS("members"),
    WARPS("warps"),
    BANNED("banned"),
    PERMISSIONS("permissions");

    private final String key;

    DynamicGuiType(String key) {
        this.key = key;
    }

    public static DynamicGuiType fromString(String raw) {
        if (raw == null || raw.isBlank()) return null;
        for (DynamicGuiType t : values()) {
            if (t.key.equalsIgnoreCase(raw) || t.name().equalsIgnoreCase(raw)) return t;
        }
        return null;
    }

    public String getKey() {
        return key;
    }
}
