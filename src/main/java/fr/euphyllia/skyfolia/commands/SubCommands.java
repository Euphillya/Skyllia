package fr.euphyllia.skyfolia.commands;

import ca.spottedleaf.concurrentutil.util.Validate;

public enum SubCommands {
    CREATE("create");

    private final String subCommandName;

    SubCommands(String subCommand) {
        this.subCommandName = subCommand;
    }

    public static SubCommands subCommandByName(String name) {
        Validate.notNull(name, "Name can not be null");
        for (SubCommands sub : SubCommands.values()) {
            if (sub.getSubCommandName().equalsIgnoreCase(name)) {
                return sub;
            }
        }
        return null;
    }

    public String getSubCommandName() {
        return subCommandName;
    }
}