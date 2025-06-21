package fr.euphyllia.skyllia.api.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public interface SkylliaCommand {
    LiteralArgumentBuilder<?> buildCommand(SkylliaCommandContext context);

    default String permission() { return ""; }
    default String description() { return ""; }
    default String usage() { return ""; }
}