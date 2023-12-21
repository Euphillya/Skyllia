package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import fr.euphyllia.skyfolia.utils.WorldEditUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreateSubCommand implements SubCommandInterface {

    private final Main plugin;
    private final Logger logger = LogManager.getLogger(this);

    public CreateSubCommand(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (sender instanceof Player player) {
                player.setGameMode(GameMode.SPECTATOR);
                Bukkit.getAsyncScheduler().runNow(this.plugin, scheduledTask -> {
                    SkyblockManager skyblockManager = new SkyblockManager(this.plugin);
                    Island island = skyblockManager.getIslandByOwner(player).join();
                    if (island != null) {
                        player.sendMessage("Vous avez déjà une île");
                    } else {
                        island = skyblockManager.createIsland(player).join();
                        player.sendMessage("L'ile en création");
                        Location center = RegionUtils.getCenterRegion(Bukkit.getWorld(ConfigToml.worldConfigs.stream().findFirst().get().name()), island.getPosition().regionX(), island.getPosition().regionZ());
                        Bukkit.getServer().getRegionScheduler().run(plugin, center, t -> {
                            WorldEditUtils.pasteSchematicWE(this.plugin.getInterneAPI(), center, ConfigToml.islandTypes.get("example"));
                            player.teleportAsync(center);
                            player.setGameMode(GameMode.SURVIVAL);
                        });
                    }
                });
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            sender.sendMessage("Impossible de créer l'ile, vérifier les logs !");
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
