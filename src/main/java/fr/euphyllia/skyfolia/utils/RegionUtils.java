package fr.euphyllia.skyfolia.utils;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.utils.models.CallbackLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class RegionUtils {
    public static Location getCenterRegion(World w, int regionX, int regionZ){
        double rx = (regionX << 9) + 256d;
        double rz = (regionZ << 9) + 256d;
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

    public static void editBlockRegion(World world, int regionX, int regionZ, Main plugin, CallbackLocation callback) {
        int minChunkX = regionX << 5;
        int minChunkZ = regionZ << 5;

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();


        int maxChunkX = 32;
        int maxChunkZ = 32;

        for(int cx = 0; cx < maxChunkX; cx++) {
            for(int cz=0; cz < maxChunkZ; cz++){
                int minX = (minChunkX + cx) << 4;
                int maxX = minX + 15;

                int minZ = (minChunkZ + cz) << 4;
                int maxZ = minZ + 15;

                for(int x = minX; x <= maxX; x++){
                    for(int z = minZ; z <= maxZ; z++) {
                        for(int y = minY; y <= maxY; y++) {
                            Location loc = new Location(world, x, y, z);
                            Bukkit.getServer().getRegionScheduler()
                                    .runDelayed(plugin, loc, t -> {
                                if(callback != null){
                                    callback.run(loc);
                                }
                            }, 2000);
                        }
                    }
                }
            }
        }
    }
}
