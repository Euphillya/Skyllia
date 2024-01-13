package fr.euphyllia.skyfolia.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyfolia.Main;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LanguageToml {

    private static final Logger logger = LogManager.getLogger(LanguageToml.class);
    public static CommentedFileConfig config;
    public static int version;
    public static String messageIslandInProgress = "L'île est en cours de création";
    public static String messageIslandCreateFinish = "Bienvenue sur votre île !";
    public static String messageOnlyOwnerCanDeleteIsland = "Désolé, seul le propriétaire peut exécuter cette commande";
    public static String messageIslandDeleteSuccess = "L'île a été supprimé avec succès";
    public static String messagePlayerHasNotIsland = "Vous n'avez pas d'île !";
    public static String messageIslandAlreadyExist = "Vous avez déjà une île.";
    public static String messagePlayerNotFound = "Le joueurs est introuvable.";
    public static String messagePlayerNotInIsland = "Vous devez être sur votre île.";
    public static String messageWarpCreate = "Votre warp : %s a été crée.";
    public static String messageError = "Une erreur s'est produite. Merci de contacter un administrateur.";
    public static String messageTransfertSuccess = "Le nouveau propriétaire de l'ile est : %new_owner%";
    public static String messageOnlyOwner = "Seul le propriétaire de l'île peut faire ça.";
    public static String messageNotMember = "Le joueur n'est pas membre de l'ile";
    public static String messageIslandTypeNotExist = "Le type d'île sélectionné n'existe pas.";
    public static String messagePlayerPermissionDenied = "Vous n'avez pas la permission de faire cela.";
    public static String messageIslandError = "Une erreur s'est produite lors de la création de l'ile";
    private static boolean verbose;

    //Below is the Chinese translation of the code above
    /*
    public static String messageIslandInProgress = "空岛正在创建中";
    public static String messageIslandCreateFinish = "欢迎来到你的空岛";
    public static String messageOnlyOwnerCanDeleteIsland = "抱歉，只有所有者才能执行此命令";
    public static String messageIslandDeleteSuccess = "空岛已成功删除";
    public static String messagePlayerHasNotIsland = "您没有空岛！";
    public static String messageIslandAlreadyExist = "您已经拥有一个空岛";
    public static String messagePlayerNotFound = "找不到玩家";
    public static String messagePlayerNotInIsland = "您必须在您自己的空岛上";
    public static String messageWarpCreate = "您的传送点：%s 已创建";
    public static String messageError = "发生错误。请联系管理员";
    public static String messageTransfertSuccess = "空岛的新所有者是：%new_owner%";
    public static String messageOnlyOwner = "只有空岛所有者才能这样做";
    public static String messageNotMember = "玩家不是空岛成员";
    public static String messageIslandTypeNotExist = "所选空岛类型不存在";
    public static String messagePlayerPermissionDenied = "您没有执行此操作的权限";
    public static String messageIslandError = "创建空岛时出现错误";
    */
    
    //Below is the English translation of the code above
    /*
    public static String messageIslandInProgress = "The island is in progress of creation.";
    public static String messageIslandCreateFinish = "Welcome to your island!";
    public static String messageOnlyOwnerCanDeleteIsland = "Sorry, only the owner can execute this command.";
    public static String messageIslandDeleteSuccess = "The island has been successfully deleted.";
    public static String messagePlayerHasNotIsland = "You do not have an island!";
    public static String messageIslandAlreadyExist = "You already have an island.";
    public static String messagePlayerNotFound = "Player not found.";
    public static String messagePlayerNotInIsland = "You must be on your island.";
    public static String messageWarpCreate = "Your warp: %s has been created.";
    public static String messageError = "An error has occurred. Please contact an administrator.";
    public static String messageTransfertSuccess = "The new owner of the island is: %new_owner%.";
    public static String messageOnlyOwner = "Only the owner of the island can do that.";
    public static String messageNotMember = "The player is not a member of the island.";
    public static String messageIslandTypeNotExist = "The selected island type does not exist.";
    public static String messagePlayerPermissionDenied = "You do not have permission to do that.";
    public static String messageIslandError = "An error occurred while creating the island.";
    */

    public static void init(File configFile) {
        config = CommentedFileConfig.builder(configFile).autosave().build();
        config.load();
        verbose = getBoolean("verbose", false);

        version = getInt("config-version", 1);
        set("config-version", 1);
        logger.log(Level.FATAL, "Lecture des config");
        try {
            readConfig(LanguageToml.class, null);
        } catch (Exception e) {
            logger.log(Level.FATAL, "Erreur de lecture !", e);
        }
    }

    protected static void log(Level level, String message) {
        if (verbose) {
            logger.log(level, message);
        }
    }

    private static void readConfig(@NotNull Class<?> clazz, Object instance) throws InvocationTargetException, IllegalAccessException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())
                    && (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE)) {
                method.setAccessible(true);
                method.invoke(instance);
            }
        }
    }

    private static void set(@NotNull String path, Object val) {
        config.set(path, val);
    }

    private static String getString(@NotNull String path, String def) {
        Object tryIt = config.get(path);
        if (tryIt == null && def != null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    private static Boolean getBoolean(@NotNull String path, boolean def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    private static Integer getInt(@NotNull String path, Integer def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.getInt(path);
    }

    private static void changeOwnerLanguage() {
        messageTransfertSuccess = getString("island.transfert.success", messageTransfertSuccess);
    }

    private static void createIslandLanguage() {
        messageIslandInProgress = getString("island.create.in-progress", messageIslandInProgress);
        messageIslandCreateFinish = getString("island.create.finish", messageIslandCreateFinish);
        messageIslandTypeNotExist = getString("island.create.type-no-exist", messageIslandTypeNotExist);
        messageIslandError = getString("island.create.error", messageIslandError);
    }

    public static String messageDemotePlayer = "Le joueur %s a été rétrogradé.";
    public static String messageDemotePlayerFailed = "Le joueur %s ne peut pas être rétrogradé.";
    public static String messageDemotePlayerFailedHighOrEqualsStatus = "Vous ne pouvez pas rétrograder un joueur de votre rang ou d'un rang plus élevé.";
    public static String messageDemoteCommandNotEnoughArgs = "La commande n'est pas complète : /skyfolia demote <member>";
    public static String messagePromotePlayer = "Le joueur %s a été promu.";
    public static String messagePromotePlayerFailed = "Le joueur %s ne peut pas être promu.";
    public static String messagePromotePlayerFailedLowOrEqualsStatus = "Vous ne pouvez pas promouvoir un joueur à votre rang ou d'un rang plus élevé.";
    public static String messagePromoteCommandNotEnoughArgs = "La commande n'est pas complète : /skyfolia promote <member>";
    private static void changeStatusPlayerLanguage() {
        // Demote
        messageDemotePlayer = getString("island.demote.success", messageDemotePlayer);
        messageDemotePlayerFailed = getString("island.demote.fail", messageDemotePlayerFailed);
        messageDemoteCommandNotEnoughArgs = getString("island.demote.not-enough-args", messageDemoteCommandNotEnoughArgs);
        messageDemotePlayerFailedHighOrEqualsStatus = getString("island.demote.fail-high-equals-status", messageDemotePlayerFailedHighOrEqualsStatus);
        // Promote
        messagePromotePlayer = getString("island.promote.success", messagePromotePlayer);
        messagePromotePlayerFailed = getString("island.promote.fail", messagePromotePlayerFailed);
        messagePromoteCommandNotEnoughArgs = getString("island.promote.not-enough-args", messagePromoteCommandNotEnoughArgs);
        messagePromotePlayerFailedLowOrEqualsStatus = getString("island.promote.fail-high-equals-status", messagePromotePlayerFailedLowOrEqualsStatus);
    }

    public static void deleteIslandLanguage() {
        messageOnlyOwnerCanDeleteIsland = getString("island.delete.only-owner", messageOnlyOwnerCanDeleteIsland);
        messageIslandDeleteSuccess = getString("island.delete.success", messageIslandDeleteSuccess);
    }

    private static void islandGenericLanguage() {
        messagePlayerHasNotIsland = getString("island.generic.player.no-island", messagePlayerHasNotIsland);
        messageIslandAlreadyExist = getString("island.generic.player.already-exist", messageIslandAlreadyExist);
        messagePlayerNotFound = getString("island.generic.player.not-found", messagePlayerNotFound);
        messagePlayerNotInIsland = getString("island.generic.player.not-in-island", messagePlayerNotInIsland);
        messageWarpCreate = getString("island.generic.player.warp-create", messageWarpCreate);
        messageError = getString("island.generic.error", messageError);
        messageOnlyOwner = getString("island.generic.only-owner", messageOnlyOwner);
        messageNotMember = getString("island.generic.not-member", messageNotMember);
        messagePlayerPermissionDenied = getString("island.generic.player.permission-denied", messagePlayerPermissionDenied);
    }


    public static void sendMessage(Main plugin, Entity entity, String msg) {
        if (msg.isEmpty()) return;
        entity.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(msg));
    }
}
