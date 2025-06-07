package fr.euphyllia.skyllia.api.commands;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface SkylliaCommandSender {

    void sendMessage(Component message);
    String getName();
    UUID getUuid();
    boolean hasPermission(String permission);

}
