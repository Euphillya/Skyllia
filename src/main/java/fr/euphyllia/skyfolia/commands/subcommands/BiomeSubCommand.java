package fr.euphyllia.skyfolia.commands.subcommands;

import com.sk89q.worldedit.world.biome.BiomeType;
import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import fr.euphyllia.skyfolia.utils.WorldEditUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BiomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(this);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (true) {
                throw new RuntimeException("Bugué ! Ne pas utiliser.");
            }
           Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
               String selectBiome = args[0].toUpperCase();
               SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();

               Island island = skyblockManager.getIslandByOwner(player).join();

               if (island != null) {
                   World world = player.getWorld();
                   WorldEditUtils.changeBiome(plugin, island, world, Biome.valueOf(selectBiome), player);
               } else {
                   // Besoin de creer une ile
                   player.sendMessage("Faut créer une ile");
               }
           });

            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> biomesList = new ArrayList<>();
        for (Biome biome : Biome.values()){
            biomesList.add(biome.name());
        }
        return biomesList;
    }
}
