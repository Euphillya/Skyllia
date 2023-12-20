package fr.euphyllia.skyfolia.utils;

import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import org.bukkit.Location;
import org.bukkit.World;

public class RegionUtils {
    public static Location getCenterRegion(World w, int regionX, int regionZ){
        double rx = (regionX << 9) + 256;
        double rz = (regionZ << 9) + 256;
        return new Location(w, rx, 0.0d, rz);
    }

    public static Position getPosition(int start) {
        double r = Math.floor((Math.sqrt(start + 1d) - 1) / 2) + 1;
        double p = (8 * r * (r - 1)) / 2;
        double en = r * 2;
        double a = (start - p) % (r * 8);
        int loc = (int) Math.floor(a / (r * 2));
        int regionX = 0;
        int regionZ = switch (loc) {
            case 0 -> {
                regionX = (int) (a - r);
                yield (int) (-r);
            }
            case 1 -> {
                regionX = (int) r;
                yield (int) ((a % en) - r);
            }
            case 2 -> {
                regionX = (int) (r - (a % en));
                yield (int) r;
            }
            case 3 -> {
                regionX = (int) (-r);
                yield (int) (r - (a % en));
            }
            default -> throw new RuntimeException("A problem with the generation of the island position has occurred.");
        };
        return new Position(regionX, regionZ);
    }
}
