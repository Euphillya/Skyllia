package fr.euphyllia.skyllia.commands.common;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SubCommandImpl extends SubCommandRegistry {

    private static final Map<String, SubCommandInterface> commandMap = new HashMap<>();

    @Override
    public void registerSubCommand(@NotNull SubCommandInterface subCommand, @NotNull String... aliases) {
        for (String alias : aliases) {
            commandMap.put(alias.toLowerCase(), subCommand);
        }
    }

    @Override
    public SubCommandInterface getSubCommandByName(@NotNull String name) {
        return commandMap.get(name.toLowerCase());
    }

    @Override
    public Map<String, SubCommandInterface> getCommandMap() {
        return commandMap;
    }
}
