package fr.euphyllia.skylliapanel.configuration;

import fr.euphyllia.skylliapanel.action.ActionDefinition;

import java.util.List;

public record ButtonDefinition(
        int slot,
        String material,
        List<ActionDefinition> actions
) {
}
