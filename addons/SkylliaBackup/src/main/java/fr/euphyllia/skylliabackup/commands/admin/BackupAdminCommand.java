package fr.euphyllia.skylliabackup.commands.admin;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skylliabackup.SkylliaBackup;
import fr.euphyllia.skylliabackup.configuration.BackupConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackupAdminCommand implements SubCommandInterface {

    private final SkylliaBackup plugin;

    public BackupAdminCommand(SkylliaBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (!PlayerUtils.hasPermission(sender, permission())) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(sender, "addons.backup.admin.usage");
            return;
        }

        String target = args[0].trim();

        if (target.equalsIgnoreCase("all")) {
            ConfigLoader.language.sendMessage(sender, "addons.backup.admin.all-started");

            Bukkit.getAsyncScheduler().runNow(this.plugin, task -> {
                int count = this.plugin.getBackupManager().backupAllIslands();
                ConfigLoader.language.sendMessage(sender, "addons.backup.admin.all-done",
                        Map.of("%count%", String.valueOf(count)));
            });

        } else {
            UUID targetId = Bukkit.getPlayerUniqueId(target);
            if (targetId == null) {
                targetId = Bukkit.getOfflinePlayer(target).getUniqueId();
            }
            if (targetId == null) {
                ConfigLoader.language.sendMessage(sender, "island.admin.player-not-found",
                        Map.of("%player%", target));
                return;
            }

            final UUID finalTargetId = targetId;
            Island island = SkylliaAPI.getIslandByPlayerId(finalTargetId);
            if (island == null) {
                ConfigLoader.language.sendMessage(sender, "island.player.no-island");
                return;
            }

            ConfigLoader.language.sendMessage(sender, "addons.backup.admin.started",
                    Map.of("%player%", target));

            Bukkit.getAsyncScheduler().runNow(this.plugin, task -> {
                File result = this.plugin.getBackupManager().backupIsland(island, "admin-" + sender.getName());
                if (result != null) {
                    ConfigLoader.language.sendMessage(sender, "addons.backup.admin.success",
                            Map.of("%player%", target, "%file%", result.getName()));
                    if (BackupConfigLoader.config.isUploadEnabled()) {
                        ConfigLoader.language.sendMessage(sender, "addons.backup.player.uploading");
                    }
                } else {
                    ConfigLoader.language.sendMessage(sender, "addons.backup.admin.failed",
                            Map.of("%player%", target));
                }
            });
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (!PlayerUtils.hasPermission(sender, permission())) return List.of();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            suggestions.add("all");
            Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName())
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .forEach(suggestions::add);
            return suggestions;
        }
        return List.of();
    }

    @Override
    public String permission() {
        return "skyllia.admins.commands.backup";
    }
}