package fr.euphyllia.skyfolia.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class RegionUtils {
    public static Location getCenterRegion(World w, int regionX, int regionZ){
        double rx = (regionX << 9) + 256;
        double rz = (regionZ << 9) + 256;
        return new Location(w, rx, 0.0d, rz);
    }
}
