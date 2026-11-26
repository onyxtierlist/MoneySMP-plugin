package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public final class PayCommand implements CommandExecutor, TabCompleter {
    private final EconomyManager economy;

    public PayCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (!player.hasPermission("moneysmp.pay")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /pay <player> <amount>");
            return true;
        }

        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null) {
            player.sendMessage(ChatColor.RED + "That player must be online to receive money.");
            return true;
        }

        if (recipient.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot pay yourself.");
            return true;
        }

        final long amountCents;
        try {
            BigDecimal amount = new BigDecimal(args[1]).setScale(2, RoundingMode.UNNECESSARY);
            amountCents = amount.movePointRight(2).longValueExact();
            if (amountCents <= 0) {
                throw new ArithmeticException("Amount must be positive.");
            }
        } catch (NumberFormatException | ArithmeticException exception) {
            player.sendMessage(ChatColor.RED + "Enter a valid amount greater than $0.00 (up to 2 decimal places).");
            return true;
        }

        if (!economy.transfer(player.getUniqueId(), recipient.getUniqueId(), amountCents)) {
            player.sendMessage(ChatColor.RED + "Payment failed. Check your balance and try again.");
            return true;
        }

        String formatted = "$" + EconomyManager.format(amountCents);
        player.sendMessage(ChatColor.GREEN + "You paid " + recipient.getName() + " " + formatted + ".");
        recipient.sendMessage(ChatColor.GREEN + "You received " + formatted + " from " + player.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
