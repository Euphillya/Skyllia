package fr.euphyllia.skylliaore.commands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliaore.Main;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.config.DefaultConfig;
import fr.euphyllia.skylliaore.database.MariaDBInit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OreCommands implements SubCommandInterface {

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skylliaore.use")) return true;
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /skylliaadmin generator <player> <generator>").color(NamedTextColor.RED));
            return false;
        }

        Bukkit.getAsyncScheduler().runNow(Main.getPlugin(Main.class), task -> {
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(args[0]);
            CompletableFuture<@Nullable Island> future = SkylliaAPI.getIslandByPlayerId(offPlayer.getUniqueId());
            if (future == null) {
                sender.sendMessage(Component.text("No island found.").color(NamedTextColor.RED));
                return;
            }
            Island island = future.join();
            if (island == null) {
                sender.sendMessage(Component.text("No island found.").color(NamedTextColor.RED));
                return;
            }

            String nameGenerator = args[1];
            CompletableFuture<Boolean> updateFuture = MariaDBInit.getMariaDbGenerator().updateGenIsland(island.getId(), nameGenerator);
            if (updateFuture.join()) {
                sender.sendMessage(Component.text("Generator changed successfully.").color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("An error occurred while changing the generator.").color(NamedTextColor.RED));
            }
        });

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        // ---------- ARG #1 : Noms de joueurs ----------
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();

            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        // ---------- ARG #2 : Liste de générateurs ----------
        else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            DefaultConfig config = Main.getDefaultConfig();
            Map<String, Generator> generators = config.getGenerators();
            return generators.keySet().stream()
                    .filter(genName -> genName.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
