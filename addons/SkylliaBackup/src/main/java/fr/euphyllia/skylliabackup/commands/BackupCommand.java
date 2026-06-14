package fr.euphyllia.skylliabackup.commands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skylliabackup.SkylliaBackup;
import fr.euphyllia.skylliabackup.configuration.BackupConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackupCommand implements SubCommandInterface {

    private final SkylliaBackup plugin;

    public BackupCommand(SkylliaBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "addons.backup.player.player-only");
            return;
        }

        if (!PlayerUtils.hasPermission(player, permission())) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        if (!BackupConfigLoader.config.isAllowPlayerBackup()) {
            ConfigLoader.language.sendMessage(player, "addons.backup.player.disabled");
            return;
        }

        UUID playerId = player.getUniqueId();

        // Cooldown
        long remaining = this.plugin.getBackupManager().getRemainingCooldown(playerId);
        if (remaining > 0) {
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            ConfigLoader.language.sendMessage(player, "addons.backup.player.cooldown",
                    Map.of("%minutes%", String.valueOf(minutes), "%seconds%", String.valueOf(seconds)));
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(playerId);
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return;
        }

        ConfigLoader.language.sendMessage(player, "addons.backup.player.started");

        Bukkit.getAsyncScheduler().runNow(this.plugin, task -> {
            File result = this.plugin.getBackupManager().backupIsland(island, player.getName());
            if (result != null) {
                this.plugin.getBackupManager().stampCooldown(playerId);
                ConfigLoader.language.sendMessage(player, "addons.backup.player.success",
                        Map.of("%file%", result.getName()));
                if (BackupConfigLoader.config.isUploadEnabled()) {
                    ConfigLoader.language.sendMessage(player, "addons.backup.player.uploading");
                }
            } else {
                ConfigLoader.language.sendMessage(player, "addons.backup.player.failed");
            }
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "skyllia.island.backup";
    }
}