package dev.moneysmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BankCommand implements CommandExecutor {
    private final EconomyManager economy;
    private final BankManager bank;

    public BankCommand(EconomyManager economy, BankManager bank) {
        this.economy = economy;
        this.bank = bank;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use the bank.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("balance")) {
            player.sendMessage("§6Bank balance: §e$" + EconomyManager.format(bank.getBalance(player.getUniqueId())));
            player.sendMessage("§7Wallet: §e$" + EconomyManager.format(economy.getBalanceCents(player.getUniqueId())));
            return true;
        }
        if (args.length != 2 || !(args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("withdraw"))) {
            player.sendMessage("§cUsage: /bank [balance] or /bank <deposit|withdraw> <amount>");
            return true;
        }
        long cents;
        try {
            BigDecimal amount = new BigDecimal(args[1]);
            if (amount.scale() > 2 || amount.signum() <= 0) throw new NumberFormatException();
            cents = amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            player.sendMessage("§cEnter a valid positive amount with up to two decimal places.");
            return true;
        }

        if (args[0].equalsIgnoreCase("deposit")) {
            if (economy.getBalanceCents(player.getUniqueId()) < cents) {
                player.sendMessage("§cYou don't have enough money in your wallet.");
                return true;
            }
            if (!economy.withdraw(player.getUniqueId(), cents)) {
                player.sendMessage("§cThe deposit could not be completed.");
                return true;
            }
            if (!bank.deposit(player.getUniqueId(), cents)) {
                economy.deposit(player.getUniqueId(), cents);
                player.sendMessage("§cThe bank could not save your deposit. Your wallet was refunded.");
                return true;
            }
            player.sendMessage("§aDeposited $" + EconomyManager.format(cents) + " into your bank.");
        } else {
            if (bank.getBalance(player.getUniqueId()) < cents) {
                player.sendMessage("§cYou don't have enough money in your bank.");
                return true;
            }
            if (!bank.withdraw(player.getUniqueId(), cents)) {
                player.sendMessage("§cThe withdrawal could not be completed.");
                return true;
            }
            if (!economy.deposit(player.getUniqueId(), cents)) {
                bank.deposit(player.getUniqueId(), cents);
                player.sendMessage("§cYour wallet could not be credited. Your bank was refunded.");
                return true;
            }
            player.sendMessage("§aWithdrew $" + EconomyManager.format(cents) + " from your bank.");
        }
        return true;
    }
}
