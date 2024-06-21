package fr.euphyllia.skyllia.api.utils;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import org.bukkit.Bukkit;

/**
 * The VersionUtils class provides utility methods to check the server's type (Paper or Folia).
 */
public class VersionUtils {

    /**
     * Indicates if the server is running Paper.
     */
    public static final boolean IS_PAPER = checkPaper();

    /**
     * Indicates if the server is running Folia.
     */
    public static final boolean IS_FOLIA = checkFolia();

    /**
     * Checks if the server has access to the Paper API.
     * Taken from <a href="https://github.com/PaperMC/PaperLib">PaperLib</a>.
     *
     * @return True if the server is running on Paper (or its forks), false otherwise.
     */
    private static boolean checkPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
    }

    /**
     * Checks if the server has access to the Folia API.
     * Taken from <a href="https://github.com/PaperMC/Folia">Folia</a>.
     *
     * @return True if the server is running on Folia (or its forks), false otherwise.
     */
    private static boolean checkFolia() {
        return SkylliaAPI.isFolia();
    }

    /**
     * Checks if a class with the specified name exists in the classpath.
     *
     * @param className The name of the class to check.
     * @return True if the class exists, false otherwise.
     */
    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
