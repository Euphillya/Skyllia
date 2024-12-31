package fr.euphyllia.skylliabank.commands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skylliabank.EconomyManager;
import fr.euphyllia.skylliabank.SkylliaBank;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankCommand implements SubCommandInterface {

    private final Plugin plugin;
    private final Economy economy;

    public BankCommand(Plugin plugin) {
        this.plugin = plugin;
        this.economy = EconomyManager.getEconomy();
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        // Vérifier si c'est un joueur
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(
                    sender,
                    SkylliaBank.getConfiguration().getString(
                            "bank.playerOnly",
                            "<red>This command can only be used by a player."
                    )
            );
            return true;
        }

        // Récupérer l'île du joueur
        @Nullable Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.noIsland",
                            "<red>You do not have an island."
                    )
            );
            return true;
        }

        UUID islandId = island.getId();

        // Exécuter en tâche asynchrone
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            if (args.length == 0) {
                // Commande sans argument => afficher le solde
                handleBalance(player, islandId);
                return;
            }

            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "deposit" -> handleDeposit(player, islandId, args);
                case "withdraw" -> handleWithdraw(player, islandId, args);
                case "balance" -> handleBalance(player, islandId);
                default -> LanguageToml.sendMessage(
                        player,
                        SkylliaBank.getConfiguration().getString(
                                "bank.unknownCommand",
                                "<yellow>Unknown command. Use /is bank [deposit|withdraw|balance]"
                        )
                );
            }
        });

        return true;
    }

    /**
     * /is bank deposit <amount>
     */
    private void handleDeposit(Player player, UUID islandId, String[] args) {
        // Permission pour deposit
        if (!player.hasPermission("skyllia.bank.deposit")) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.noPermissionDeposit",
                            "<red>You do not have permission to deposit money."
                    )
            );
            return;
        }

        // Usage /is bank deposit <amount>
        if (args.length < 2) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.usageDeposit",
                            "<yellow>Usage: /is bank deposit <amount>"
                    )
            );
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                LanguageToml.sendMessage(
                        player,
                        SkylliaBank.getConfiguration().getString(
                                "bank.invalidAmountPositive",
                                "<red>The amount must be positive."
                        )
                );
                return;
            }
        } catch (NumberFormatException e) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.invalidAmount",
                            "<red>Invalid amount."
                    )
            );
            return;
        }

        // Vérifier argent du joueur
        if (economy.getBalance(player) < amount) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.notEnoughMoneyPlayer",
                            "<red>You do not have enough money."
                    )
            );
            return;
        }

        // Retirer l'argent du joueur
        EconomyResponse withdrawResponse = economy.withdrawPlayer(player, amount);
        if (withdrawResponse.transactionSuccess()) {
            // Dépôt dans la banque de l'île
            SkylliaBank.getBankManager().deposit(islandId, amount).thenAcceptAsync(success -> {
                if (success) {
                    // successDeposit
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "bank.successDeposit",
                            "<green>Deposited %amount% into your island's bank."
                    );
                    String msg = msgTemplate.replace("%amount%", economy.format(amount));
                    LanguageToml.sendMessage(player, msg);
                } else {
                    // dépôt échoue => rembourser
                    EconomyResponse refundResponse = economy.depositPlayer(player, amount);
                    if (refundResponse.transactionSuccess()) {
                        LanguageToml.sendMessage(
                                player,
                                SkylliaBank.getConfiguration().getString(
                                        "bank.errorDepositRefunded",
                                        "<red>An error occurred. Your money has been refunded."
                                )
                        );
                    } else {
                        plugin.getLogger().severe("Deposit and refund both failed for player " + player.getName());
                        LanguageToml.sendMessage(
                                player,
                                SkylliaBank.getConfiguration().getString(
                                        "bank.criticalErrorDeposit",
                                        "<red>Critical error: deposit failed, refund also failed. Contact an administrator."
                                )
                        );
                    }
                    plugin.getLogger().severe("Error depositing to island " + islandId);
                }
            }).exceptionally(ex -> {
                // Exception => rembourser
                EconomyResponse refundResponse = economy.depositPlayer(player, amount);
                if (refundResponse.transactionSuccess()) {
                    LanguageToml.sendMessage(
                            player,
                            SkylliaBank.getConfiguration().getString(
                                    "bank.errorDepositRefunded",
                                    "<red>An error occurred. Your money has been refunded."
                            )
                    );
                } else {
                    plugin.getLogger().severe("Deposit and refund both failed for player " + player.getName() + " : " + ex.getMessage());
                    LanguageToml.sendMessage(
                            player,
                            SkylliaBank.getConfiguration().getString(
                                    "bank.criticalErrorDeposit",
                                    "<red>Critical error: deposit failed, refund also failed. Contact an administrator."
                            )
                    );
                }
                plugin.getLogger().severe("Exception depositing to island " + islandId + " : " + ex.getMessage());
                return null;
            });
        } else {
            // Retrait échoue
            String errorMsg = SkylliaBank.getConfiguration().getString(
                    "bank.errorWithdrawPlayer",
                    "<red>Withdrawal error: %errorMessage%"
            );
            LanguageToml.sendMessage(
                    player,
                    errorMsg.replace("%errorMessage%", withdrawResponse.errorMessage)
            );
        }
    }

    /**
     * /is bank withdraw <amount>
     */
    private void handleWithdraw(Player player, UUID islandId, String[] args) {
        // Permission pour withdraw
        if (!player.hasPermission("skyllia.bank.withdraw")) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.noPermissionWithdraw",
                            "<red>You do not have permission to withdraw money."
                    )
            );
            return;
        }

        // Usage /is bank withdraw <amount>
        if (args.length < 2) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.usageWithdraw",
                            "<yellow>Usage: /is bank withdraw <amount>"
                    )
            );
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                LanguageToml.sendMessage(
                        player,
                        SkylliaBank.getConfiguration().getString(
                                "bank.invalidAmountPositive",
                                "<red>The amount must be positive."
                        )
                );
                return;
            }
        } catch (NumberFormatException e) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.invalidAmount",
                            "<red>Invalid amount."
                    )
            );
            return;
        }

        // Vérifier le solde de l'île
        SkylliaBank.getBankManager().getBankAccount(islandId).thenAcceptAsync(bankAccount -> {
            if (bankAccount.balance() < amount) {
                LanguageToml.sendMessage(
                        player,
                        SkylliaBank.getConfiguration().getString(
                                "bank.notEnoughMoneyIsland",
                                "<red>Your island bank does not have enough money."
                        )
                );
                return;
            }

            // Retirer de l'île
            SkylliaBank.getBankManager().withdraw(islandId, amount).thenAcceptAsync(success -> {
                if (success) {
                    // Déposer au joueur
                    EconomyResponse depositResponse = economy.depositPlayer(player, amount);
                    if (depositResponse.transactionSuccess()) {
                        String msgTemplate = SkylliaBank.getConfiguration().getString(
                                "bank.successWithdraw",
                                "<green>Withdrew %amount% from your island's bank."
                        );
                        LanguageToml.sendMessage(
                                player,
                                msgTemplate.replace("%amount%", economy.format(amount))
                        );
                    } else {
                        // Échec => rembourser la banque
                        SkylliaBank.getBankManager().deposit(islandId, amount).thenAcceptAsync(refundSuccess -> {
                            if (refundSuccess) {
                                LanguageToml.sendMessage(
                                        player,
                                        SkylliaBank.getConfiguration().getString(
                                                "bank.errorDepositPlayerRefundIsland",
                                                "<red>Error depositing to your wallet. The island bank has been refunded."
                                        )
                                );
                            } else {
                                plugin.getLogger().severe("Failed deposit to player & refund for island " + islandId);
                                LanguageToml.sendMessage(
                                        player,
                                        SkylliaBank.getConfiguration().getString(
                                                "bank.criticalErrorWithdraw",
                                                "<red>Critical error: deposit to your wallet and island refund both failed. Contact an administrator."
                                        )
                                );
                            }
                        }).exceptionally(refundEx -> {
                            plugin.getLogger().severe("Exception refunding the island bank " + islandId + " : " + refundEx.getMessage());
                            LanguageToml.sendMessage(
                                    player,
                                    SkylliaBank.getConfiguration().getString(
                                            "bank.criticalErrorWithdraw",
                                            "<red>Critical error: deposit to your wallet failed, and island bank refund also failed. Contact an administrator."
                                    )
                            );
                            return null;
                        });
                    }
                } else {
                    LanguageToml.sendMessage(
                            player,
                            SkylliaBank.getConfiguration().getString(
                                    "bank.errorWithdrawIsland",
                                    "<red>An error occurred while withdrawing from your island bank."
                            )
                    );
                }
            }).exceptionally(ex -> {
                plugin.getLogger().severe("Error withdrawing from island " + islandId + " : " + ex.getMessage());
                LanguageToml.sendMessage(
                        player,
                        SkylliaBank.getConfiguration().getString(
                                "bank.errorGenericWithdraw",
                                "<red>An error occurred during the withdrawal. Please try again later."
                        )
                );
                return null;
            });
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Error checking island balance " + islandId + " : " + ex.getMessage());
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "error",
                            "<red>An error has occurred."
                    )
            );
            return null;
        });
    }

    /**
     * /is bank balance
     */
    private void handleBalance(Player player, UUID islandId) {
        // Permission pour voir le solde
        if (!player.hasPermission("skyllia.bank.balance")) {
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "bank.noPermissionBalance",
                            "<red>You do not have permission to view the balance."
                    )
            );
            return;
        }

        SkylliaBank.getBankManager().getBankAccount(islandId).thenAcceptAsync(bankAccount -> {
            if (bankAccount != null) {
                String msgTemplate = SkylliaBank.getConfiguration().getString(
                        "bank.balance",
                        "<green>Your island's bank balance: %amount%"
                );
                LanguageToml.sendMessage(
                        player,
                        msgTemplate.replace("%amount%", economy.format(bankAccount.balance()))
                );
            } else {
                LanguageToml.sendMessage(
                        player,
                        SkylliaBank.getConfiguration().getString(
                                "bank.errorGetBalance",
                                "<red>Unable to retrieve your island's balance."
                        )
                );
            }
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Error retrieving island " + islandId + " balance: " + ex.getMessage());
            LanguageToml.sendMessage(
                    player,
                    SkylliaBank.getConfiguration().getString(
                            "error",
                            "<red>An error has occurred."
                    )
            );
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
