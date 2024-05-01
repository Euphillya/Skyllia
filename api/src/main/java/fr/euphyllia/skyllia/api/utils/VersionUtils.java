package fr.euphyllia.skyllia.api.utils;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import org.bukkit.Bukkit;

public class VersionUtils {

    /**
     * Checks if the server is Paper
     */
    public static final boolean IS_PAPER = checkPaper();

    /**
     * Checks if the server is Folia
     */
    public static final boolean IS_FOLIA = checkFolia();

    /**
     * Check if the server has access to the Paper API
     * Taken from <a href="https://github.com/PaperMC/PaperLib">PaperLib</a>
     *
     * @return True if on Paper server (or forks), false anything else
     */
    private static boolean checkPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
    }

    /**
     * Check if the server has access to the Folia API
     * Taken from <a href="https://github.com/PaperMC/Folia">Folia</a>
     *
     * @return True if on Folia server (or forks), false anything else
     */
    private static boolean checkFolia() {
        return SkylliaAPI.isFolia();
    }


    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
