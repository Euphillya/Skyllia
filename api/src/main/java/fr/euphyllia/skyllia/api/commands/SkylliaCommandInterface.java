package fr.euphyllia.skyllia.api.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public interface SkylliaCommandInterface extends BasicCommand {

    @Nullable String permission();
}
