package fr.euphyllia.skyllia.api.commands;

public interface SkylliaCommandContext {
    SkylliaCommandSender getSender();
    String[] getArgs();
}