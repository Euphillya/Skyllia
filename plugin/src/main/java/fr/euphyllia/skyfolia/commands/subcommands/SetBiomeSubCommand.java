package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.WorldEditUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SetBiomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetBiomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (true) {
            throw new RuntimeException("Bugué ! Ne pas utiliser.");
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                String selectBiome = args[0].toUpperCase();
                SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();

                Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

                if (island == null) {
                    player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messagePlayerHasNotIsland));
                    return;
                }

                World world = player.getWorld();
                WorldEditUtils.changeBiome(plugin, island, world, Biome.valueOf(selectBiome), player);
            });
        } finally {
            executor.shutdown();
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> biomesList = new ArrayList<>();
        for (Biome biome : Biome.values()) {
            biomesList.add(biome.name());
        }
        return biomesList;
    }
}
