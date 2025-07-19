package fr.euphyllia.skylliabank.commands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliabank.EconomyManager;
import fr.euphyllia.skylliabank.SkylliaBank;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankCommand implements SubCommandInterface {

    private final Plugin plugin;
    private final Economy economy;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BankCommand(Plugin plugin) {
        this.plugin = plugin;
        this.economy = EconomyManager.getEconomy();
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        // Vérifier si c'est un joueur
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "addons.bank.player.player-only");
            return true;
        }
        UUID playerId = player.getUniqueId();

        if (CommandCacheExecution.isAlreadyExecute(playerId, "bank")) {
            ConfigLoader.language.sendMessage(player, "island.generic.command-in-progress");
            return true;
        }
        CommandCacheExecution.addCommandExecute(playerId, "bank");

        // Récupérer l'île du joueur
        @Nullable Island island = SkylliaAPI.getCacheIslandByPlayerId(playerId);
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.no-island");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return true;
        }

        UUID islandId = island.getId();

        if (args.length == 0) {
            // Commande sans argument => afficher le solde
            handleBalance(player, islandId);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "deposit" -> handleDeposit(player, islandId, args);
            case "withdraw" -> handleWithdraw(player, islandId, args);
            case "balance" -> handleBalance(player, islandId);
            default -> {
                ConfigLoader.language.sendMessage(player, "addons.bank.player.unknown-command");
                CommandCacheExecution.removeCommandExec(playerId, "bank");
            }
        }


        return true;
    }

    /**
     * /is bank deposit <amount>
     */
    private void handleDeposit(Player player, UUID islandId, String[] args) {
        UUID playerId = player.getUniqueId();
        if (!PermissionImp.hasPermission(player, "skyllia.bank.deposit")) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.no-permission-deposit");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return;
        }

        if (args.length < 2) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.usage-deposit");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                ConfigLoader.language.sendMessage(player, "addons.bank.player.invalid-amount-positive");
                CommandCacheExecution.removeCommandExec(playerId, "bank");
                return;
            }
        } catch (NumberFormatException e) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.invalid-amount");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return;
        }

        if (economy.getBalance(player) < amount) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.not-enough-money-player");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return;
        }

        EconomyResponse withdrawResponse = economy.withdrawPlayer(player, amount);
        if (withdrawResponse.transactionSuccess()) {
            SkylliaBank.getBankManager().deposit(islandId, amount).thenAcceptAsync(success -> {
                if (success) {
                    ConfigLoader.language.sendMessage(player, "addons.bank.player.success-deposit", Map.of("%amount%", economy.format(amount)));
                } else {
                    EconomyResponse refundResponse = economy.depositPlayer(player, amount);
                    if (refundResponse.transactionSuccess()) {
                        ConfigLoader.language.sendMessage(player, "addons.bank.player.error-deposit-refunded");
                    } else {
                        ConfigLoader.language.sendMessage(player, "addons.bank.player.critical-error-deposit");
                    }
                }
                CommandCacheExecution.removeCommandExec(playerId, "bank");
            }).exceptionally(ex -> {
                economy.depositPlayer(player, amount);
                ConfigLoader.language.sendMessage(player, "addons.bank.player.error-deposit-refunded");
                CommandCacheExecution.removeCommandExec(playerId, "bank");
                return null;
            });
        } else {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.error-withdraw-player", Map.of("%errorMessage%", withdrawResponse.errorMessage));
            CommandCacheExecution.removeCommandExec(playerId, "bank");
        }
    }

    /**
     * /is bank withdraw <amount>
     */
    private void handleWithdraw(Player player, UUID islandId, String[] args) {
        UUID playerId = player.getUniqueId();
        if (!PermissionImp.hasPermission(player, "skyllia.bank.withdraw")) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.no-permission-withdraw");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return;
        }


        if (args.length < 2) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.usage-withdraw");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                ConfigLoader.language.sendMessage(player, "addons.bank.player.invalid-amount-positive");
                CommandCacheExecution.removeCommandExec(playerId, "bank");
                return;
            }
        } catch (NumberFormatException e) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.invalid-amount");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return;
        }

        // Vérifier le solde de l'île
        SkylliaBank.getBankManager().getBankAccount(islandId).thenAcceptAsync(bankAccount -> {
            if (bankAccount.balance() < amount) {
                ConfigLoader.language.sendMessage(player, "addons.bank.player.not-enough-money-island");
                CommandCacheExecution.removeCommandExec(playerId, "bank");
                return;
            }

            SkylliaBank.getBankManager().withdraw(islandId, amount).thenAcceptAsync(success -> {
                if (success) {
                    EconomyResponse depositResponse = economy.depositPlayer(player, amount);
                    if (depositResponse.transactionSuccess()) {
                        ConfigLoader.language.sendMessage(player, "addons.bank.player.success-withdraw", Map.of("%amount%", economy.format(amount)));
                        CommandCacheExecution.removeCommandExec(playerId, "bank");
                    } else {
                        SkylliaBank.getBankManager().deposit(islandId, amount).thenAcceptAsync(refund -> {
                            if (refund) {
                                ConfigLoader.language.sendMessage(player, "addons.bank.player.error-deposit-player-refund-island");
                            } else {
                                ConfigLoader.language.sendMessage(player, "addons.bank.player.critical-error-withdraw");
                            }
                            CommandCacheExecution.removeCommandExec(playerId, "bank");
                        });
                    }
                } else {
                    ConfigLoader.language.sendMessage(player, "addons.bank.player.error-withdraw-island");
                    CommandCacheExecution.removeCommandExec(playerId, "bank");
                }
            });
        }).exceptionally(ex -> {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.error-generic-withdraw");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return null;
        });
    }

    /**
     * /is bank balance
     */
    private void handleBalance(Player player, UUID islandId) {
        UUID playerId = player.getUniqueId();
        if (!PermissionImp.hasPermission(player, "skyllia.bank.balance")) {
            ConfigLoader.language.sendMessage(player, "addons.bank.player.no-permission-balance");
            CommandCacheExecution.removeCommandExec(player.getUniqueId(), "bank");
            return;
        }

        SkylliaBank.getBankManager().getBankAccount(islandId).thenAcceptAsync(bankAccount -> {
            if (bankAccount != null) {
                ConfigLoader.language.sendMessage(player, "addons.bank.player.balance", Map.of("%amount%", economy.format(bankAccount.balance())));
            } else {
                ConfigLoader.language.sendMessage(player, "addons.bank.player.error-get-balance");
            }
            CommandCacheExecution.removeCommandExec(playerId, "bank");
        }).exceptionally(ex -> {
            ConfigLoader.language.sendMessage(player, "addons.bank.error");
            CommandCacheExecution.removeCommandExec(playerId, "bank");
            return null;
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin,
                                               @NotNull CommandSender sender,
                                               @NotNull String[] args) {
        // ARG 1 : "deposit", "withdraw", "balance"
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> commands = Arrays.asList("deposit", "withdraw", "balance");
            return commands.stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        }
        // ARG 2 : on propose des montants
        else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            List<String> amounts = Arrays.asList("1", "10", "100", "1000");
            return amounts.stream()
                    .filter(amount -> amount.startsWith(partial))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
