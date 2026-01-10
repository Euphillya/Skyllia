package fr.euphyllia.skylliabank.commands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliabank.EconomyManager;
import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliabank.api.BankAccount;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankAdminCommand implements SubCommandInterface {

    private static final Logger log = LogManager.getLogger(BankAdminCommand.class);
    private final Plugin plugin;
    private final Economy economy;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BankAdminCommand(Plugin plugin) {
        this.plugin = plugin;
        this.economy = EconomyManager.getEconomy();
    }

    private void handleBalance(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.usage-balance");
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (offlinePlayer.getName() == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-found");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());

        if (island == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-island");
            return;
        }

        UUID islandId = island.getId();

        BankAccount bankAccount = SkylliaBank.getBankManager().getBankAccount(islandId);

        if (bankAccount != null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.island-balance", Map.of(
                    "{player_name}", offlinePlayer.getName(),
                    "{amount}", economy.format(bankAccount.balance())
            ));
        } else {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.island-error-balance", Map.of(
                    "{player_name}", offlinePlayer.getName()
            ));
        }
    }

    private void handleDeposit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.usage-deposit");
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (offlinePlayer.getName() == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-found");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());


        if (island == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-island");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                ConfigLoader.language.sendMessage(sender, "addons.bank.admin.invalid-amount-positive");
                return;
            }
        } catch (NumberFormatException e) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.invalid-amount");
            return;
        }

        UUID islandId = island.getId();
        boolean success = SkylliaBank.getBankManager().deposit(islandId, amount);

        if (success) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.deposit-success", Map.of(
                    "%amount%", economy.format(amount),
                    "{player_name}", offlinePlayer.getName()
            ));
        } else {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.deposit-error", Map.of(
                    "{player_name}", offlinePlayer.getName()
            ));
        }
    }

    private void handleWithdraw(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.usage-withdraw");
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (offlinePlayer == null || offlinePlayer.getName() == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-found");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());


        if (island == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-island");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                ConfigLoader.language.sendMessage(sender, "addons.bank.admin.invalid-amount-positive");
                return;
            }
        } catch (NumberFormatException e) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.invalid-amount");
            return;
        }

        UUID islandId = island.getId();
        boolean success = SkylliaBank.getBankManager().withdraw(islandId, amount);

        if (success) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.success-withdraw", Map.of(
                    "%amount%", economy.format(amount),
                    "{player_name}", offlinePlayer.getName()
            ));
        } else {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.error-withdraw", Map.of(
                    "{player_name}", offlinePlayer.getName()
            ));
        }
    }

    private void handleSetBalance(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.usage-set-balance");
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (offlinePlayer.getName() == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-found");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());

        if (island == null) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.player-not-island");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0) {
                ConfigLoader.language.sendMessage(sender, "addons.bank.admin.invalid-amount-negative");
                return;
            }
        } catch (NumberFormatException e) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.invalid-amount");
            return;
        }

        UUID islandId = island.getId();
        boolean success = SkylliaBank.getBankManager().setBalance(islandId, amount);

        if (success) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.set-balance-success", Map.of(
                    "{player_name}", offlinePlayer.getName(),
                    "%amount%", economy.format(amount)
            ));
        } else {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.set-balance-error", Map.of(
                    "{player_name}", offlinePlayer.getName()
            ));
        }
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.bank.admin")) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.no-permissions");
            return true;
        }

        if (args.length == 0) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.admin.usage-root");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "balance" -> handleBalance(sender, args);
            case "deposit" -> handleDeposit(sender, args);
            case "withdraw" -> handleWithdraw(sender, args);
            case "setbalance" -> handleSetBalance(sender, args);
            default -> ConfigLoader.language.sendMessage(sender, "addons.bank.admin.unknown-command");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin,
                                               @NotNull CommandSender sender,
                                               @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            List<String> commands = List.of("balance", "deposit", "withdraw", "setbalance");
            return commands.stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String command = args[0].toLowerCase();
            String partial = args[1].trim().toLowerCase();

            return switch (command) {
                case "balance", "setbalance" -> new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .sorted()
                        .collect(Collectors.toList());
                case "deposit", "withdraw" -> {
                    List<String> amounts = List.of("10", "50", "100", "500", "1000");
                    yield amounts.stream()
                            .filter(amount -> amount.startsWith(partial))
                            .sorted()
                            .collect(Collectors.toList());
                }
                default -> List.of();
            };
        }

        return List.of();
    }
}
