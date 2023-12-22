package fr.euphyllia.skyfolia.utils.models;

import org.bukkit.Location;
import org.bukkit.block.Block;

public interface CallbackLocation {
    void run(Location block);
}