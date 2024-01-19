package fr.euphyllia.skyllia.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.Main;
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
    public static String messagePlayerNotFound = "Le joueur est introuvable.";
    public static String messagePlayerNotInIsland = "Vous devez être sur votre île.";
    public static String messageWarpCreateSuccess = "Votre warp : %s a été crée.";
    public static String messageError = "Une erreur s'est produite. Merci de contacter un administrateur.";
    public static String messageTransfertSuccess = "Le nouveau propriétaire de l'ile est : %new_owner%";
    public static String messageOnlyOwner = "Seul le propriétaire de l'île peut faire ça.";
    public static String messageNotMember = "Le joueur n'est pas membre de l'ile";
    public static String messageIslandTypeNotExist = "Le type d'île sélectionné n'existe pas.";
    public static String messageIslandSchemNotExist = "La schematic pour créer l'ile n'existe pas.";
    public static String messagePlayerPermissionDenied = "Vous n'avez pas la permission de faire cela.";
    public static String messageIslandError = "Une erreur s'est produite lors de la création de l'ile";
    public static String messageDemotePlayer = "Le joueur %s a été rétrogradé.";
    public static String messageDemotePlayerFailed = "Le joueur %s ne peut pas être rétrogradé.";
    public static String messageDemotePlayerFailedHighOrEqualsStatus = "Vous ne pouvez pas rétrograder un joueur de votre rang ou d'un rang plus élevé.";
    public static String messageDemoteCommandNotEnoughArgs = "La commande n'est pas complète : /skyllia demote <member>";
    public static String messagePromotePlayer = "Le joueur %s a été promu.";
    public static String messagePromotePlayerFailed = "Le joueur %s ne peut pas être promu.";
    public static String messagePromotePlayerFailedLowOrEqualsStatus = "Vous ne pouvez pas promouvoir un joueur à votre rang ou d'un rang plus élevé.";
    public static String messagePromoteCommandNotEnoughArgs = "La commande n'est pas complète : /skyllia promote <member>";
    public static String messageCommandAlreadyExecution = "La commande est déjà en cours d'execution, veuillez patienter quelques instants.";
    public static String messageBiomeCommandNotEnoughArgs = "La commande n'est pas complète : /skyllia biome <biome>";
    public static String messageBiomeOnlyIsland = "La commande ne peut être exécuté seulement sur une île";
    public static String messageBiomeNotExist = "Le biome %s n'existe pas.";
    public static String messageBiomeChangeInProgress = "Changement de biome en cours. Veuillez notez que ça prends du temps... Un message vous avertira quand le processus sera achevé.";
    public static String messageBiomeChangeSuccess = "Le changement de biome dans le chunk où vous étiez est terminé !";
    public static String messageInviteAlreadyIsland = "Vous êtes déjà sur une île !";
    public static String messageInviteCommandNotEnoughArgs = "La commande n'est pas complète : /skyllia invite <add/accept/decline> <player/island_owner>";
    public static String messageInviteAcceptCommandNotEnoughArgs = "Vous devez préciser sur quel île vous souhaiter rejoindre : /skyllia invite accept <island_owner>";
    public static String messageInviteDeclineCommandNotEnoughArgs = "Vous devez préciser sur quel île vous souhaiter décliner : /skyllia invite decline <island_owner>";
    public static String messageInviteAddCommandNotEnoughArgs = "Vous devez préciser sur quel île vous souhaiter décliner : /skyllia invite add <player>";
    public static String messageInvitePlayerInvited = "Le joueur %s a bien été invité. En attente d'une réponse...";
    public static String messageInvitePlayerNotification = "Le joueur %player_invite% vous a invité sur son île. Pour accepter : /skyllia invite accept %player_invite%. Pour décliner : /skyllia invite decline %player_invite%";
    public static String messageInviteAcceptOwnerHasNotIsland = "L'île du joueur %s n'a pas été trouvé.";
    public static String messageInviteDeclineOwnerHasNotIsland = "L'île du joueur %s n'a pas été trouvé.";
    public static String messageInviteJoinIsland = "Vous êtes dorénavant membre de l'île !";
    public static String messageInviteMaxMemberExceededIsland = "Le seuil de place de membre de l'ile a été atteints. Vous ne pouvez pas rejoindre l'île.";
    public static String messageInviteDeclineDeleteInvitation = "Vous avez refusé l'invitation de %player_invite%.";
    public static String messageKickPlayerSuccess = "Le joueur a été viré de votre île.";
    public static String messageKickPlayerFailed = "Le joueur n'a pas pu être viré. Si le problème persiste, contacter un administrateur";
    public static String messageKickPlayerFailedHighOrEqualsStatus = "Vous ne pouvez pas virer un joueur de votre rang ou d'un rang plus élevé.";
    public static String messageLeaveFailedIsOwnerIsland = "Vous ne pouvez pas quitter votre île, car vous être le propriétaire !";
    public static String messageLeaveSuccess = "Vous avez quitté votre île.";
    public static String messageLeavePlayerFailed = "Vous n'avez pas pu quitter votre île. Si le problème persiste, contacter un administrateur";
    public static String messageAccessIslandOpen = "Votre île est maintenant ouverte.";
    public static String messageAccessIslandClose = "Votre île est maintenant fermée.";
    public static String messageHomeIslandSuccess = "Vous avez été téléporté sur votre île.";
    public static String messageHomeCreateSuccess = "Vous avez modifier votre home !";
    public static String messageWarpCommandNotEnoughArgs = "Vous devez préciser le nom du warp à enregistrer : /skyllia <set/del>warp <warp_name>";
    public static String messageIslandNotDeleteHome = "Vous ne pouvez pas supprimer le warp home.";
    public static String messageWarpDeleteSuccess = "Le warp a été supprimé.";
    public static String messageWarpNotExist = "Le warp demandé n'existe pas.";
    public static String messageWarpTeleportSuccess = "Vous avez été téléporter sur le warp demandé.";
    public static String messageVisitCommandNotEnoughArgs = "Vous devez le nom de l'île que vous souhaitez visiter : /skyllia visit <player>";
    public static String messageVisitPlayerHasNotIsland = "Le joueur n'a pas d'île";
    public static String messageVisitIslandIsPrivate = "L'île est fermée.";
    public static String messageVisitIslandSuccess = "Vous avez été téléporté sur l'île de %player%.";
    public static String messageExpelPlayerFailed = "Le joueur ne peut être expulser de votre île.";
    public static String messageExpelPlayerFailedNotInIsland = "Le joueur n'est pas sur votre île.";
    public static String messageExpelCommandNotEnoughArgs = "La commande n'est pas complète : /skyllia expel <player>";
    public static String messagePlayerNotConnected = "Le joueur n'est pas connecté";
    public static String messageLocationNotSafe = "La localisation n'est pas sure ! Téléportation impossible.";
    public static String messagePermissionCommandNotEnoughArgs = "La commande est incomplete : /skyllia permission <island/commands/inventory> <OWNER/CO_OWNER/MODERATOR/MEMBER/VISITOR/BAN> <PERMISSION_NAME> <true/false>";
    public static String messagePermissionPermissionTypeInvalid = "Le type est invalide, possibilité : <island/commands/inventory>";
    public static String messagePermissionRoleTypeInvalid = "Le role est invalide, possibilité : <OWNER/CO_OWNER/MODERATOR/MEMBER/VISITOR/BAN>";
    public static String messagePermissionsPermissionsValueInvalid = "Cette permission n'existe pas.";
    public static String messagePermissionsUpdateSuccess = "La permission a été mise à jour !";
    public static String messagePermissionsUpdateFailed = "Le changement n'a pas pu être réalisé.";
    public static String messagePermissionPlayerFailedHighOrEqualsStatus = "Vous ne pouvez pas modifier votre propres permissions ou les permissions des roles supérieur à vous.";
    private static boolean verbose;

    public static void init(File configFile) {
        config = CommentedFileConfig.builder(configFile).autosave().build();
        config.load();
        verbose = getBoolean("verbose", false);

        version = getInt("config-version", 1);
        set("config-version", 1);
        if (verbose) {
            logger.log(Level.INFO, "Lecture du fichier langue");
        }
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

    private static void accessIslandLanguage() {
        messageAccessIslandOpen = getString("island.access.open", messageAccessIslandOpen);
        messageAccessIslandClose = getString("island.access.close", messageAccessIslandClose);
    }

    private static void changeOwnerLanguage() {
        messageTransfertSuccess = getString("island.transfert.success", messageTransfertSuccess);
    }

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

    private static void biomeLanguage() {
        messageBiomeCommandNotEnoughArgs = getString("island.biome.not-enough-args", messageBiomeCommandNotEnoughArgs);
        messageBiomeOnlyIsland = getString("island.biome.only-island", messageBiomeOnlyIsland);
        messageBiomeNotExist = getString("island.biome.biome-not-exist", messageBiomeNotExist);
        messageBiomeChangeInProgress = getString("island.biome.change-in-progress", messageBiomeChangeInProgress);
        messageBiomeChangeSuccess = getString("island.biome.success", messageBiomeChangeSuccess);
    }

    private static void createIslandLanguage() {
        messageIslandInProgress = getString("island.create.in-progress", messageIslandInProgress);
        messageIslandCreateFinish = getString("island.create.finish", messageIslandCreateFinish);
        messageIslandTypeNotExist = getString("island.create.type-no-exist", messageIslandTypeNotExist);
        messageIslandSchemNotExist = getString("island.create.schem-no-exist", messageIslandSchemNotExist);
        messageIslandError = getString("island.create.error", messageIslandError);
    }

    private static void deleteIslandLanguage() {
        messageOnlyOwnerCanDeleteIsland = getString("island.delete.only-owner", messageOnlyOwnerCanDeleteIsland);
        messageIslandDeleteSuccess = getString("island.delete.success", messageIslandDeleteSuccess);
    }

    private static void expelIslandLanguage() {
        messageExpelCommandNotEnoughArgs = getString("island.expel.not-enough-args", messageExpelCommandNotEnoughArgs);
        messageExpelPlayerFailed = getString("island.expel.player-failed", messageExpelPlayerFailed);
        messageExpelPlayerFailedNotInIsland = getString("island.expel.player-not-in-island", messageExpelPlayerFailedNotInIsland);
    }

    private static void homeIslandLanguage() {
        messageHomeIslandSuccess = getString("island.home.success", messageHomeIslandSuccess);
        messageHomeCreateSuccess = getString("island.home.set.success", messageHomeCreateSuccess);
    }

    private static void islandGenericLanguage() {
        messagePlayerHasNotIsland = getString("island.generic.player.no-island", messagePlayerHasNotIsland);
        messageIslandAlreadyExist = getString("island.generic.player.already-exist", messageIslandAlreadyExist);
        messagePlayerNotFound = getString("island.generic.player.not-found", messagePlayerNotFound);
        messagePlayerNotInIsland = getString("island.generic.player.not-in-island", messagePlayerNotInIsland);
        messageError = getString("island.generic.error", messageError);
        messageOnlyOwner = getString("island.generic.only-owner", messageOnlyOwner);
        messageNotMember = getString("island.generic.not-member", messageNotMember);
        messagePlayerPermissionDenied = getString("island.generic.player.permission-denied", messagePlayerPermissionDenied);
        messageCommandAlreadyExecution = getString("island.generic.player.command-already-execution", messageCommandAlreadyExecution);
        messagePlayerNotConnected = getString("island.generic.player.offline", messagePlayerNotConnected);
        messageLocationNotSafe = getString("island.generic.location.not-safe", messageLocationNotSafe);
    }

    private static void islandInviteLanguage() {
        messageInviteAlreadyIsland = getString("island.invite.already-on-an-island", messageInviteAlreadyIsland);
        messageInviteCommandNotEnoughArgs = getString("island.invite.not-enough-args", messageInviteCommandNotEnoughArgs);
        messageInviteAcceptCommandNotEnoughArgs = getString("island.invite.accept.not-enough-args", messageInviteAcceptCommandNotEnoughArgs);
        messageInviteDeclineCommandNotEnoughArgs = getString("island.invite.decline.not-enough-args", messageInviteDeclineCommandNotEnoughArgs);
        messageInviteAddCommandNotEnoughArgs = getString("island.invite.add.not-enough-args", messageInviteAddCommandNotEnoughArgs);
        messageInvitePlayerNotification = getString("island.invite.add.notification-player", messageInvitePlayerNotification);
        messageInvitePlayerInvited = getString("island.invite.add.pending", messageInvitePlayerInvited);
        messageInviteJoinIsland = getString("island.invite.accept.success", messageInviteJoinIsland);
        messageInviteMaxMemberExceededIsland = getString("island.invite.accept.max-member-exceeded", messageInviteMaxMemberExceededIsland);
        messageInviteAcceptOwnerHasNotIsland = getString("island.invite.accept.owner-not-island", messageInviteAcceptOwnerHasNotIsland);
        messageInviteDeclineOwnerHasNotIsland = getString("island.invite.decline.owner-not-island", messageInviteDeclineOwnerHasNotIsland);
    }

    private static void islandKickLanguage() {
        messageKickPlayerSuccess = getString("island.kick.success", messageInviteJoinIsland);
        messageKickPlayerFailed = getString("island.kick.failed", messageKickPlayerFailed);
        messageKickPlayerFailedHighOrEqualsStatus = getString("island.kick.fail-high-equals-status", messageKickPlayerFailedHighOrEqualsStatus);
    }

    private static void islandLeaveLanguage() {
        messageLeaveSuccess = getString("island.leave.success", messageLeaveSuccess);
        messageLeavePlayerFailed = getString("island.leave.failed", messageLeavePlayerFailed);
        messageLeaveFailedIsOwnerIsland = getString("island.leave.he-is-owner", messageLeaveFailedIsOwnerIsland);
    }

    private static void islandPermissionLanguage() {
        messagePermissionCommandNotEnoughArgs = getString("island.permissions.not-enough-args", messagePermissionCommandNotEnoughArgs);
        messagePermissionPermissionTypeInvalid = getString("island.permissions.permission-type-invalid", messagePermissionPermissionTypeInvalid);
        messagePermissionRoleTypeInvalid = getString("island.permissions.role-invalid", messagePermissionRoleTypeInvalid);
        messagePermissionsPermissionsValueInvalid = getString("island.permissions.permissions-invalid", messagePermissionsPermissionsValueInvalid);
        messagePermissionsUpdateSuccess = getString("island.permissions.update.success", messagePermissionsUpdateSuccess);
        messagePermissionsUpdateFailed = getString("island.permissions.update.failed", messagePermissionsUpdateFailed);
        messagePermissionPlayerFailedHighOrEqualsStatus = getString("island.permission.fail-high-equals-status", messagePermissionPlayerFailedHighOrEqualsStatus);
    }

    private static void islandWarpLanguage() {
        messageWarpCommandNotEnoughArgs = getString("island.warp.not-enough-args", messageWarpCommandNotEnoughArgs);
        messageWarpCreateSuccess = getString("island.warp.success", messageWarpCreateSuccess);
        messageIslandNotDeleteHome = getString("island.warp.delete.can-not-delete-home", messageIslandNotDeleteHome);
        messageWarpDeleteSuccess = getString("island.warp.delete.success", messageWarpDeleteSuccess);
        messageWarpNotExist = getString("island.warp.teleport.not-exist", messageWarpNotExist);
        messageWarpTeleportSuccess = getString("island.warp.teleport.success", messageWarpTeleportSuccess);
    }

    private static void islandVisitLanguage() {
        messageVisitCommandNotEnoughArgs = getString("island.visit.not-enough-args", messageVisitCommandNotEnoughArgs);
        messageVisitPlayerHasNotIsland = getString("island.visit.player-not-island", messageVisitPlayerHasNotIsland);
        messageVisitIslandIsPrivate = getString("island.visit.island-not-open", messageVisitIslandIsPrivate);
        messageVisitIslandSuccess = getString("island.visit.success", messageVisitIslandSuccess);
    }


    public static void sendMessage(Main plugin, Entity entity, String msg) {
        if (msg.isEmpty()) return;
        entity.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(msg));
    }
}
