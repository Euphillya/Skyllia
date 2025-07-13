package fr.euphyllia.skylliaore.commands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.config.DefaultConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OreCommands implements SubCommandInterface {

    private SkylliaOre plugin;

    public OreCommands(SkylliaOre plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skylliaore.use")) return true;
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /skylliaadmin generator <player> <generator>").color(NamedTextColor.RED));
            return false;
        }

        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(args[0]);
        CompletableFuture<@Nullable Island> future = SkylliaAPI.getIslandByPlayerId(offPlayer.getUniqueId());
        if (future == null) {
            sender.sendMessage(Component.text("No island found.").color(NamedTextColor.RED));
            return true;
        }

        final String nameGenerator = args[1];
        Generator generator = SkylliaOre.getDefaultConfig().getGenerators().get(nameGenerator);

        if (generator == null) {
            sender.sendMessage(Component.text("The generator '" + nameGenerator + "' does not exist.").color(NamedTextColor.RED));
            return true;
        }


        future.thenAcceptAsync(island -> {
            if (island == null) {
                sender.sendMessage(Component.text("No island found.").color(NamedTextColor.RED));
                return;
            }

            SkylliaOre.getGeneratorManager().updateGenerator(island.getId(), generator.name()).thenAccept(success -> {
                if (success) {
                    SkylliaOre.updateGeneratorCache(island.getId(), generator);
                    sender.sendMessage(Component.text("Generator changed to '" + generator.name() + "'.").color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("An error occurred while changing the generator.").color(NamedTextColor.RED));
                }
            });

        }).exceptionally(throwable -> {
            sender.sendMessage(Component.text("An error occurred while retrieving the island.").color(NamedTextColor.RED));
            return null;
        });

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        // ---------- ARG #1 : Noms de joueurs ----------
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();

            return new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ---------- ARG #2 : Liste de générateurs ----------
        else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            DefaultConfig config = SkylliaOre.getDefaultConfig();
            Map<String, Generator> generators = config.getGenerators();
            return generators.keySet().stream()
                    .filter(genName -> genName.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
