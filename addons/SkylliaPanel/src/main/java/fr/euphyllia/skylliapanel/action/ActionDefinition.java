package fr.euphyllia.skylliapanel.action;

public record ActionDefinition(ActionType type, String value) {

    public ActionDefinition {
        if (type == null) {
            throw new IllegalArgumentException("Action type cannot be null");
        }
        if (value == null || value.isBlank()) {
            value = "";
        }
    }
}
