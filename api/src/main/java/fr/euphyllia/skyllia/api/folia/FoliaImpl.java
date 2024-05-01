package fr.euphyllia.skyllia.api.folia;

import fr.euphyllia.energie.Energie;
import net.minecraft.server.level.ServerLevel;

public class FoliaImpl {

    public static void ensureGlobalTickThread() {
        if (Energie.isFolia()) {
            io.papermc.paper.threadedregions.RegionizedServer.ensureGlobalTickThread("World create can be done only on global tick thread");
        }
    }

    public static void addWorldFolia(ServerLevel internal) {
        if (Energie.isFolia()) {
            io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(internal);
        }
    }
}
