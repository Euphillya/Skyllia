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
    public static String messageIslandInProgress = "The island is being created";
    public static String messageIslandCreateFinish = "Welcome to your island!";
    public static String messageOnlyOwnerCanDeleteIsland = "Sorry, only the owner can execute this command";
    public static String messageIslandDeleteSuccess = "The island has been successfully deleted";
    public static String messagePlayerHasNotIsland = "You don't have an island!";
    public static String messageIslandAlreadyExist = "You already have an island.";
    public static String messagePlayerNotFound = "Player not found.";
    public static String messagePlayerNotInIsland = "You must be on your island.";
    public static String messageWarpCreateSuccess = "Your warp: %s has been created.";
    public static String messageError = "An error occurred. Please contact an administrator.";
    public static String messageTransfertSuccess = "The new owner of the island is: %new_owner%";
    public static String messageOnlyOwner = "Only the island owner can do this.";
    public static String messageNotMember = "The player is not a member of the island";
    public static String messageIslandTypeNotExist = "The selected island type does not exist.";
    public static String messageIslandSchemNotExist = "The schematic to create the island does not exist.";
    public static String messagePlayerPermissionDenied = "You do not have permission to do this.";
    public static String messageIslandError = "An error occurred during island creation";
    public static String messageDemotePlayer = "The player %s has been demoted.";
    public static String messageDemotePlayerFailed = "The player %s cannot be demoted.";
    public static String messageDemotePlayerFailedHighOrEqualsStatus = "You cannot demote a player of your rank or higher.";
    public static String messageDemoteCommandNotEnoughArgs = "The command is incomplete: /skyllia demote <member>";
    public static String messagePromotePlayer = "The player %s has been promoted.";
    public static String messagePromotePlayerFailed = "The player %s cannot be promoted.";
    public static String messagePromotePlayerFailedLowOrEqualsStatus = "You cannot promote a player to your rank or higher.";
    public static String messagePromoteCommandNotEnoughArgs = "The command is incomplete: /skyllia promote <member>";
    public static String messageCommandAlreadyExecution = "The command is already being executed, please wait a moment.";
    public static String messageBiomeCommandNotEnoughArgs = "The command is incomplete: /skyllia biome <biome>";
    public static String messageBiomeOnlyIsland = "The command can only be executed on an island";
    public static String messageBiomeNotExist = "The biome %s does not exist.";
    public static String messageBiomePermissionDenied = "You do not have permission to use this biome.";
    public static String messageBiomeChangeInProgress = "Biome change in progress. Please note that it takes time... A message will notify you when the process is complete.";
    public static String messageBiomeChangeSuccess = "The biome change in the chunk you were in is complete! You need to leave and return to your island to see the change.";
    public static String messageInviteAlreadyIsland = "You are already on an island!";
    public static String messageInviteCommandNotEnoughArgs = "The command is incomplete: /skyllia invite <add/accept/decline> <player/island_owner>";
    public static String messageInviteAcceptCommandNotEnoughArgs = "You must specify which island you want to join: /skyllia invite accept <island_owner>";
    public static String messageInviteDeclineCommandNotEnoughArgs = "You must specify which island you want to decline: /skyllia invite decline <island_owner>";
    public static String messageInviteAddCommandNotEnoughArgs = "You must specify which island you want to decline: /skyllia invite add <player>";
    public static String messageInvitePlayerInvited = "The player %s has been invited. Awaiting a response...";
    public static String messageInvitePlayerNotification = "The player %player_invite% has invited you to their island. To accept: /skyllia invite accept %player_invite%. To decline: /skyllia invite decline %player_invite%";
    public static String messageInviteAcceptOwnerHasNotIsland = "The island of the player %s was not found.";
    public static String messageInviteDeclineOwnerHasNotIsland = "The island of the player %s was not found.";
    public static String messageInviteJoinIsland = "You are now a member of the island!";
    public static String messageInviteMaxMemberExceededIsland = "The member limit of the island has been reached. You cannot join the island.";
    public static String messageInviteDeclineDeleteInvitation = "You have declined the invitation from %player_invite%.";
    public static String messageKickPlayerSuccess = "The player has been kicked from your island.";
    public static String messageKickPlayerFailed = "The player could not be kicked. If the problem persists, contact an administrator";
    public static String messageKickPlayerFailedHighOrEqualsStatus = "You cannot kick a player of your rank or higher.";
    public static String messageBanCommandNotEnoughArgs = "The command is incomplete: /skyllia ban <player>";
    public static String messageLeaveFailedIsOwnerIsland = "You cannot leave your island, as you are the owner!";
    public static String messageLeaveSuccess = "You have left your island.";
    public static String messageLeavePlayerFailed = "You could not leave your island. If the problem persists, contact an administrator";
    public static String messageAccessIslandOpen = "Your island is now open.";
    public static String messageAccessIslandClose = "Your island is now closed.";
    public static String messageHomeIslandSuccess = "You have been teleported to your island.";
    public static String messageHomeCreateSuccess = "You have modified your home!";
    public static String messageWarpCommandNotEnoughArgs = "You must specify the name of the warp to register: /skyllia <set/del>warp <warp_name>";
    public static String messageIslandNotDeleteHome = "You cannot delete the home warp.";
    public static String messageWarpDeleteSuccess = "The warp has been deleted.";
    public static String messageWarpNotExist = "The requested warp does not exist.";
    public static String messageWarpTeleportSuccess = "You have been teleported to the requested warp.";
    public static String messageVisitCommandNotEnoughArgs = "You must specify the name of the island you want to visit: /skyllia visit <player>";
    public static String messageVisitPlayerHasNotIsland = "The player does not have an island";
    public static String messageVisitIslandIsPrivate = "The island is closed.";
    public static String messageVisitIslandPlayerBanned = "You are banned from the island.";
    public static String messageVisitIslandSuccess = "You have been teleported to the island of %player%.";
    public static String messageExpelPlayerFailed = "The player cannot be expelled from your island.";
    public static String messageExpelPlayerFailedNotInIsland = "The player is not on your island.";
    public static String messageExpelCommandNotEnoughArgs = "The command is incomplete: /skyllia expel <player>";
    public static String messagePlayerNotConnected = "The player is not connected";
    public static String messageLocationNotSafe = "The location is not safe! Teleportation impossible.";
    public static String messagePermissionCommandNotEnoughArgs = "The command is incomplete: /skyllia permission <island/commands/inventory> <OWNER/CO_OWNER/MODERATOR/MEMBER/VISITOR/BAN> <PERMISSION_NAME> <true/false>";
    public static String messagePermissionPermissionTypeInvalid = "The type is invalid, possible values: <island/commands/inventory>";
    public static String messagePermissionRoleTypeInvalid = "The role is invalid, possible values: <OWNER/CO_OWNER/MODERATOR/MEMBER/VISITOR/BAN>";
    public static String messagePermissionsPermissionsValueInvalid = "This permission does not exist.";
    public static String messagePermissionsUpdateSuccess = "The permission has been updated!";
    public static String messagePermissionsUpdateFailed = "The change could not be made.";
    public static String messagePermissionPlayerFailedHighOrEqualsStatus = "You cannot modify your own permissions or the permissions of roles higher than you.";
    public static String messageBanImpossiblePlayerInIsland = "The player cannot be banned as they are a member of your island.";
    public static String messageBanPlayerSuccess = "The player has been banned from your island.";
    public static String messageKickCommandNotEnoughArgs = "The command is incomplete: /skyllia kick <player>";
    public static String messageUnbanCommandNotEnoughArgs = "The command is incomplete: /skyllia unban <player>";
    public static String messageUnbanPlayerNotBanned = "The player is not banned.";
    public static String messageUnBanPlayerSuccess = "The player has been unbanned.";
    public static String messageUnbanPlayerFailed = "The player could not be unbanned for an unknown reason.";
    public static String messageADeleteCommandNotEnoughArgs = "The player's name is missing.";
    public static String messageADeleteNotConfirmedArgs = "You must add 'confirm' to the end of your command";
    public static String messageASetSizeCommandNotEnoughArgs = "The command is incomplete: /skylladmin setsize <player/uuid> <number> confirm";
    public static String messageASetSizeNotConfirmedArgs = "You must add the argument 'confirm' to the end of your command.";
    public static String messageASetSizeNAN = "You did not choose an integer.";
    public static String messageASetSizeFailed = "The change could not be made.";
    public static String messageASetSizeSuccess = "The number of people on the island has been changed.";
    public static String messageTrustSuccess = "The player has been added to your trust list until the next restart or until you remove them. Note that a trusted person has as many permissions as a member of your island.";
    public static String messageTrustCommandNotEnoughArgs = "The command is incomplete: /skyllia trust <player>";
    public static String messageUntrustFailed = "An error occurred while removing the person from your trust list. Were they really trusted on your island?";
    public static String messageUntrustSuccess = "The member is no longer in your trusted list";
    public static String messageUntrustCommandNotEnoughArgs = "The command is incomplete: /skyllia untrust <player>";
    public static String messageASetMaxMembersCommandNotEnoughArgs = "The command is incomplete: /skyllia setmaxmembers value confirm";
    public static String messageASetMaxMembersNotConfirmedArgs = "You did not add 'confirm' to the end";
    public static String messageASetMaxMembersNAN = "The indicated number is incorrect.";
    public static String messageGameRuleCommandNotEnoughArgs = "The command is incomplete: /skyllia gamerule <GAMERULE> <true/false>";
    public static String messageGameRuleInvalid = "The gamerule does not exist";
    public static String messageGameRuleUpdateSuccess = "The gamerule has been updated";
    public static String messageGameRuleUpdateFailed = "An error occurred while updating the gamerule";
    private static boolean verbose;

    public static void init(File configFile) {
        config = CommentedFileConfig.builder(configFile).sync().autosave().build();
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

    private static void adminLanguage() {
        // forcedelete
        messageADeleteCommandNotEnoughArgs = getString("admin.commands.island.delete.not-enough-args", messageADeleteCommandNotEnoughArgs);
        messageADeleteNotConfirmedArgs = getString("admin.commands.island.delete.no-confirm", messageADeleteNotConfirmedArgs);
        // setMember
        messageASetSizeCommandNotEnoughArgs = getString("admin.commands.island.setsize.not-enough-args", messageASetSizeCommandNotEnoughArgs);
        messageASetSizeSuccess = getString("admin.commands.island.setsize.success", messageASetSizeSuccess);
        messageASetSizeFailed = getString("admin.commands.island.setsize.failed", messageASetSizeFailed);
        messageASetSizeNAN = getString("admin.commands.island.setsize.nan", messageASetSizeNAN);
        messageASetSizeNotConfirmedArgs = getString("admin.commands.island.setsize.no-confirm", messageASetSizeNotConfirmedArgs);
        // setSize
        messageASetMaxMembersCommandNotEnoughArgs = getString("admin.commands.island.setmaxmembers.not-enough-args", messageASetMaxMembersCommandNotEnoughArgs);
        messageASetMaxMembersNotConfirmedArgs = getString("admin.commands.island.setmaxmembers.no-confirm", messageASetMaxMembersNotConfirmedArgs);
        messageASetMaxMembersNAN = getString("admin.commands.island.setmaxmembers.nan", messageASetMaxMembersNAN);

    }

    private static void islandAccessLanguage() {
        messageAccessIslandOpen = getString("island.access.open", messageAccessIslandOpen);
        messageAccessIslandClose = getString("island.access.close", messageAccessIslandClose);
    }

    private static void islandChangeOwnerLanguage() {
        messageTransfertSuccess = getString("island.transfert.success", messageTransfertSuccess);
    }

    private static void islandChangeStatusPlayerLanguage() {
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

    private static void islandBanLanguage() {
        messageBanCommandNotEnoughArgs = getString("island.ban.not-enough-args", messageBanCommandNotEnoughArgs);
        messageBanImpossiblePlayerInIsland = getString("island.ban.failed-player-in-island", messageBanImpossiblePlayerInIsland);
        messageBanPlayerSuccess = getString("island.ban.success", messageBanPlayerSuccess);
    }

    private static void islandBiomeLanguage() {
        messageBiomeCommandNotEnoughArgs = getString("island.biome.not-enough-args", messageBiomeCommandNotEnoughArgs);
        messageBiomeOnlyIsland = getString("island.biome.only-island", messageBiomeOnlyIsland);
        messageBiomeNotExist = getString("island.biome.biome-not-exist", messageBiomeNotExist);
        messageBiomePermissionDenied = getString("island.biome.permission-denied", messageBiomePermissionDenied);
        messageBiomeChangeInProgress = getString("island.biome.change-in-progress", messageBiomeChangeInProgress);
        messageBiomeChangeSuccess = getString("island.biome.success", messageBiomeChangeSuccess);
    }

    private static void islandCreateLanguage() {
        messageIslandInProgress = getString("island.create.in-progress", messageIslandInProgress);
        messageIslandCreateFinish = getString("island.create.finish", messageIslandCreateFinish);
        messageIslandTypeNotExist = getString("island.create.type-no-exist", messageIslandTypeNotExist);
        messageIslandSchemNotExist = getString("island.create.schem-no-exist", messageIslandSchemNotExist);
        messageIslandError = getString("island.create.error", messageIslandError);
    }

    private static void islandDeleteLanguage() {
        messageOnlyOwnerCanDeleteIsland = getString("island.delete.only-owner", messageOnlyOwnerCanDeleteIsland);
        messageIslandDeleteSuccess = getString("island.delete.success", messageIslandDeleteSuccess);
    }

    private static void islandExpelLanguage() {
        messageExpelCommandNotEnoughArgs = getString("island.expel.not-enough-args", messageExpelCommandNotEnoughArgs);
        messageExpelPlayerFailed = getString("island.expel.player-failed", messageExpelPlayerFailed);
        messageExpelPlayerFailedNotInIsland = getString("island.expel.player-not-in-island", messageExpelPlayerFailedNotInIsland);
    }

    private static void islandHomeLanguage() {
        messageHomeIslandSuccess = getString("island.home.success", messageHomeIslandSuccess);
        messageHomeCreateSuccess = getString("island.home.set.success", messageHomeCreateSuccess);
    }

    private static void islandGameRuleLanguage() {
        messageGameRuleCommandNotEnoughArgs = getString("island.gamerule.not-enough-args", messageGameRuleCommandNotEnoughArgs);
        messageGameRuleInvalid = getString("island.gamerule.gamerule-invalid", messageGameRuleInvalid);
        messageGameRuleUpdateSuccess = getString("island.gamerule.success", messageGameRuleUpdateSuccess);
        messageGameRuleUpdateFailed = getString("island.gamerule.failed", messageGameRuleUpdateFailed);
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
        messageKickCommandNotEnoughArgs = getString("island.kick.not-enough-args", messageKickCommandNotEnoughArgs);
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

    private static void islandTrustLanguage() {
        messageTrustSuccess = getString("island.trust.sucess", messageTrustSuccess);
        messageTrustCommandNotEnoughArgs = getString("island.trust.not-enough-args", messageTrustCommandNotEnoughArgs);
    }

    private static void islandUnbanLanguage() {
        messageUnbanCommandNotEnoughArgs = getString("island.unban.not-enough-args", messageUnbanCommandNotEnoughArgs);
        messageUnbanPlayerNotBanned = getString("island.unban.player-not-banned", messageUnbanPlayerNotBanned);
        messageUnBanPlayerSuccess = getString("island.unban.success", messageUnBanPlayerSuccess);
        messageUnbanPlayerFailed = getString("island.unban.failed", messageUnbanPlayerFailed);
    }

    private static void islandUntrustLanguage() {
        messageUntrustCommandNotEnoughArgs = getString("island.untrust.not-enough-args", messageUntrustCommandNotEnoughArgs);
        messageUntrustSuccess = getString("island.untrust.success", messageUntrustSuccess);
        messageUntrustCommandNotEnoughArgs = getString("island.untrust.failed", messageUntrustCommandNotEnoughArgs);
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
        messageVisitIslandPlayerBanned = getString("island.visit.player-banned", messageVisitIslandPlayerBanned);
    }


    public static void sendMessage(Main plugin, Entity entity, String msg) {
        if (msg.isEmpty()) return;
        entity.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(msg));
    }
}
