package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldEditUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeleteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DeleteSubCommand.class);

    public static void checkClearPlayer(Main plugin, SkyblockManager skyblockManager, Players players, RemovalCause cause) {
        Player bPlayer = Bukkit.getPlayer(players.getMojangId());
        if (bPlayer != null && bPlayer.isOnline()) {
            PlayerUtils.teleportPlayerSpawn(bPlayer);
            bPlayer.getScheduler().execute(plugin, () -> {
                switch (cause) {
                    case KICKED -> {
                        if (ConfigToml.clearInventoryWhenKickedIsland) {
                            bPlayer.getInventory().clear();
                        }
                        if (ConfigToml.clearEnderChestWhenKickedIsland) {
                            bPlayer.getEnderChest().clear();
                        }
                        if (ConfigToml.resetExperiencePlayerWhenKickedIsland) {
                            bPlayer.setTotalExperience(0);
                            bPlayer.sendExperienceChange(0, 0); // Mise à jour du packet
                        }
                    }
                    case ISLAND_DELETED -> {
                        if (ConfigToml.clearInventoryWhenDeleteIsland) {
                            bPlayer.getInventory().clear();
                        }
                        if (ConfigToml.clearEnderChestWhenDeleteIsland) {
                            bPlayer.getEnderChest().clear();
                        }
                        if (ConfigToml.resetExperiencePlayerWhenDeleteIsland) {
                            bPlayer.setTotalExperience(0);
                            bPlayer.sendExperienceChange(0, 0); // Mise à jour du packet
                        }
                    }
                    case LEAVE -> {
                        if (ConfigToml.clearInventoryWhenLeaveIsland) {
                            bPlayer.getInventory().clear();
                        }
                        if (ConfigToml.clearEnderChestWhenLeaveIsland) {
                            bPlayer.getEnderChest().clear();
                        }
                        if (ConfigToml.resetExperiencePlayerWhenLeaveIsland) {
                            bPlayer.setTotalExperience(0);
                            bPlayer.sendExperienceChange(0, 0);
                        }
                    }
                }
                bPlayer.setGameMode(GameMode.SURVIVAL);
            }, null, 0L);
        } else {
            skyblockManager.addClearMemberNextLogin(players.getMojangId(), cause);
        }
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.delete")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length != 1) {
            LanguageToml.sendMessage(sender, LanguageToml.messageDeleteCommandNotEnoughArgs);
            return true;
        }
        String confirm = args[0];
        if (!confirm.equalsIgnoreCase("confirm")) {
            LanguageToml.sendMessage(sender, LanguageToml.messageADeleteNotConfirmedArgs);
            return true;
        }
        try {
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
            if (island == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());

            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                LanguageToml.sendMessage(player, LanguageToml.messageOnlyOwnerCanDeleteIsland);
                return true;
            }


            boolean isDisabled = island.setDisable(true);
            if (isDisabled) {
                this.updatePlayer(Main.getPlugin(Main.class), skyblockManager, island);

                for (WorldConfig worldConfig : ConfigToml.worldConfigs) {
                    WorldEditUtils.deleteIsland(Main.getPlugin(Main.class), island, Bukkit.getWorld(worldConfig.name()));
                }

                LanguageToml.sendMessage(player, LanguageToml.messageIslandDeleteSuccess);
            } else {
                LanguageToml.sendMessage(player, LanguageToml.messageError);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    private void updatePlayer(Main plugin, SkyblockManager skyblockManager, Island island) {
        for (Players players : island.getMembers()) {
            players.setRoleType(RoleType.VISITOR);
            island.updateMember(players);
            checkClearPlayer(plugin, skyblockManager, players, RemovalCause.ISLAND_DELETED);
        }
    }
}
