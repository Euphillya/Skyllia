package fr.euphyllia.skylliabank.commands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliabank.EconomyManager;
import fr.euphyllia.skylliabank.SkylliaBank;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
        // usageBalance
        if (args.length < 2) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                            "admin.usageBalance",
                            "<yellow>Usage: /skylliadmin bank balance <player>"
                    ))
            );
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        // playerNotFound
        if (offlinePlayer == null || offlinePlayer.getName() == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotFound",
                                    "<red>The player does not exist or was not found."
                            )
                    ));
            return;
        }

        CompletableFuture<@Nullable Island> islandFuture =
                SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());

        // error
        if (islandFuture == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "error",
                                    "<red>An error has occurred."
                            )
                    ));
            return;
        }

        Island island = islandFuture.join();
        // playerNotIsland
        if (island == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotIsland",
                                    "<red>The player has no island."
                            )
                    ));
            return;
        }

        UUID islandId = island.getId();

        // Récupération du solde
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            SkylliaBank.getBankManager().getBankAccount(islandId).thenAcceptAsync(bankAccount -> {
                if (bankAccount != null) {
                    // islandBalance
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.islandBalance",
                            "<green>{player_name}'s Island Balance: <yellow>{amount}."
                    );
                    String msg = msgTemplate
                            .replace("{player_name}", offlinePlayer.getName())
                            .replace("{amount}", economy.format(bankAccount.balance()));
                    sender.sendMessage(miniMessage.deserialize(msg));
                } else {
                    // islandErrorBalance
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.islandErrorBalance",
                            "<red>Unable to recover {player_name}'s Island balance."
                    );
                    String msg = msgTemplate.replace("{player_name}", offlinePlayer.getName());
                    sender.sendMessage(miniMessage.deserialize(msg));
                }
            }).exceptionally(ex -> {
                String msgTemplate = SkylliaBank.getConfiguration().getString(
                        "admin.islandErrorBalance",
                        "<red>Unable to recover {player_name}'s Island balance."
                );
                String msg = msgTemplate.replace("{player_name}", offlinePlayer.getName());
                sender.sendMessage(miniMessage.deserialize(msg));
                log.error("An error occurred while getting island balance of {}: {}", offlinePlayer.getName(), ex.getMessage());
                return null;
            });
        });
    }

    private void handleDeposit(CommandSender sender, String[] args) {
        // usageDeposit
        if (args.length < 3) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.usageDeposit",
                                    "<yellow>Usage: /skylliadmin bank deposit <player> <amount>"
                            )
                    ));
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        // playerNotFound
        if (offlinePlayer == null || offlinePlayer.getName() == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotFound",
                                    "<red>The player does not exist or was not found."
                            )
                    ));
            return;
        }

        CompletableFuture<@Nullable Island> islandFuture =
                SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());

        // error
        if (islandFuture == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "error",
                                    "<red>An error has occurred."
                            )
                    ));
            return;
        }

        Island island = islandFuture.join();
        // playerNotIsland
        if (island == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotIsland",
                                    "<red>The player has no island."
                            )
                    ));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                // invalidAmountPositive
                sender.sendMessage(
                        miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                        "admin.invalidAmountPositive",
                                        "<red>Invalid amount. Must be positive."
                                )
                        ));
                return;
            }
        } catch (NumberFormatException e) {
            // invalidAmount
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.invalidAmount",
                                    "<red>Invalid amount."
                            )
                    ));
            return;
        }

        UUID islandId = island.getId();
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            SkylliaBank.getBankManager().deposit(islandId, amount).thenAcceptAsync(success -> {
                if (success) {
                    // depositSuccess
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.depositSuccess",
                            "<green>Deposited %amount% into {player_name}'s island bank."
                    );
                    String msg = msgTemplate
                            .replace("%amount%", economy.format(amount))
                            .replace("{player_name}", offlinePlayer.getName());
                    sender.sendMessage(miniMessage.deserialize(msg));
                } else {
                    // depositError
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.depositError",
                            "<red>An error occurred while depositing to {player_name}'s island bank."
                    );
                    String msg = msgTemplate.replace("{player_name}", offlinePlayer.getName());
                    sender.sendMessage(miniMessage.deserialize(msg));
                }
            }).exceptionally(ex -> {
                log.error("Error depositing to island bank for {}: {}", offlinePlayer.getName(), ex.getMessage());
                sender.sendMessage(
                        miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                        "error",
                                        "<red>An error has occurred."
                                )
                        ));
                return null;
            });
        });
    }

    private void handleWithdraw(CommandSender sender, String[] args) {
        // usageWithdraw
        if (args.length < 3) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.usageWithdraw",
                                    "<yellow>Usage: /skylliadmin bank withdraw <player> <amount>"
                            )
                    ));
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        // playerNotFound
        if (offlinePlayer == null || offlinePlayer.getName() == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotFound",
                                    "<red>The player does not exist or was not found."
                            )
                    ));
            return;
        }

        CompletableFuture<@Nullable Island> islandFuture =
                SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());

        // error
        if (islandFuture == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "error",
                                    "<red>An error has occurred."
                            )
                    ));
            return;
        }

        Island island = islandFuture.join();
        // playerNotIsland
        if (island == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotIsland",
                                    "<red>The player has no island."
                            )
                    ));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                // invalidAmountPositive
                sender.sendMessage(
                        miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                        "admin.invalidAmountPositive",
                                        "<red>Invalid amount. Must be positive."
                                )
                        ));
                return;
            }
        } catch (NumberFormatException e) {
            // invalidAmount
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.invalidAmount",
                                    "<red>Invalid amount."
                            )
                    ));
            return;
        }

        UUID islandId = island.getId();
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            SkylliaBank.getBankManager().withdraw(islandId, amount).thenAcceptAsync(success -> {
                if (success) {
                    // successWithdraw
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.successWithdraw",
                            "<green>Withdrawn %amount% from {player_name}'s island bank."
                    );
                    String msg = msgTemplate
                            .replace("%amount%", economy.format(amount))
                            .replace("{player_name}", offlinePlayer.getName());
                    sender.sendMessage(miniMessage.deserialize(msg));
                } else {
                    // errorWithdraw
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.errorWithdraw",
                            "<red>An error occurred while withdrawing from {player_name}'s island bank."
                    );
                    String msg = msgTemplate.replace("{player_name}", offlinePlayer.getName());
                    sender.sendMessage(miniMessage.deserialize(msg));
                }
            }).exceptionally(ex -> {
                log.error("Error withdrawing from island bank for {}: {}", offlinePlayer.getName(), ex.getMessage());
                sender.sendMessage(
                        miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                        "error",
                                        "<red>An error has occurred."
                                )
                        ));
                return null;
            });
        });
    }

    private void handleSetBalance(CommandSender sender, String[] args) {
        // usageSetBalance
        if (args.length < 3) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.usageSetBalance",
                                    "<yellow>Usage: /skylliadmin bank setbalance <player> <amount>"
                            )
                    ));
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        // playerNotFound
        if (offlinePlayer == null || offlinePlayer.getName() == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotFound",
                                    "<red>The player does not exist or was not found."
                            )
                    ));
            return;
        }

        CompletableFuture<@Nullable Island> islandFuture =
                SkylliaAPI.getIslandByPlayerId(offlinePlayer.getUniqueId());

        // error
        if (islandFuture == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "error",
                                    "<red>An error has occurred."
                            )
                    ));
            return;
        }

        Island island = islandFuture.join();
        // playerNotIsland
        if (island == null) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.playerNotIsland",
                                    "<red>The player has no island."
                            )
                    ));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0) {
                sender.sendMessage(
                        miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                        "admin.invalidAmountNegative",
                                        "<red>Amount cannot be negative."
                                )
                        ));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "admin.invalidAmount",
                                    "<red>Invalid amount."
                            )
                    ));
            return;
        }

        UUID islandId = island.getId();
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            SkylliaBank.getBankManager().setBalance(islandId, amount).thenAcceptAsync(success -> {
                if (success) {
                    // setBalanceSuccess
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.setBalanceSuccess",
                            "<green>Set {player_name}'s island bank balance to %amount%."
                    );
                    String msg = msgTemplate
                            .replace("{player_name}", offlinePlayer.getName())
                            .replace("%amount%", economy.format(amount));
                    sender.sendMessage(miniMessage.deserialize(msg));
                } else {
                    // setBalanceError
                    String msgTemplate = SkylliaBank.getConfiguration().getString(
                            "admin.setBalanceError",
                            "<red>An error occurred while setting {player_name}'s island balance."
                    );
                    String msg = msgTemplate.replace("{player_name}", offlinePlayer.getName());
                    sender.sendMessage(miniMessage.deserialize(msg));
                }
            }).exceptionally(ex -> {
                log.error("Error setting island balance for player {}: {}", offlinePlayer.getName(), ex.getMessage());
                sender.sendMessage(
                        miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                        "error",
                                        "<red>An error has occurred."
                                )
                        ));
                return null;
            });
        });
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        // noPermission
        if (!PermissionImp.hasPermission(sender, "skyllia.bank.admin")) {
            sender.sendMessage(
                    miniMessage.deserialize(SkylliaBank.getConfiguration().getString(
                                    "noPermission",
                                    "<red>You do not have permission to perform this action."
                            )
                    ));
            return true;
        }

        // /skylliadmin bank ...
        if (args.length == 0) {
            // commande inconnue → petit rappel
            sender.sendMessage(miniMessage.deserialize("<yellow>Usage: /skylliadmin bank <balance|deposit|withdraw|setbalance> <player> [amount]"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "balance" -> handleBalance(sender, args);
            case "deposit" -> handleDeposit(sender, args);
            case "withdraw" -> handleWithdraw(sender, args);
            case "setbalance" -> handleSetBalance(sender, args);
            default ->
                    sender.sendMessage(miniMessage.deserialize("<yellow>Unknown command. Usage: /skylliadmin bank <balance|deposit|withdraw|setbalance> <player> [amount]"));
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
