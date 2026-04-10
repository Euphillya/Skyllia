package fr.euphyllia.skylliapanel.action;

import java.util.Arrays;

public enum ActionType {

    OPEN_GUI("open_gui"),
    COMMAND_PLAYER("command_player"),
    COMMAND_SERVER("command_server"),
    CLOSE("close");

    private final String key;

    ActionType(String key) {
        this.key = key;
    }

    public static ActionType fromString(String raw) {
        for (ActionType type : values()) {
            if (type.key.equalsIgnoreCase(raw) || type.name().equalsIgnoreCase(raw)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown action type: " + raw + ". Please use one of: " + String.join(", ", Arrays.toString(values())));
    }

    public String getKey() {
        return key;
    }
}
