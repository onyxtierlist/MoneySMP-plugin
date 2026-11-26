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
import java.util.List;

public final class EcoCommand implements CommandExecutor, TabCompleter {
    private final EconomyManager economy;

    public EcoCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moneysmp.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /eco <give|take|set> <player> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        if (!List.of("give", "take", "set").contains(action)) {
            sender.sendMessage(ChatColor.RED + "Choose give, take, or set.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "That player must be online.");
            return true;
        }

        final long amountCents;
        try {
            BigDecimal amount = new BigDecimal(args[2]).setScale(2, RoundingMode.UNNECESSARY);
            amountCents = amount.movePointRight(2).longValueExact();
            if (amountCents < 0 || (amountCents == 0 && !action.equals("set"))) {
                throw new ArithmeticException("Invalid amount.");
            }
        } catch (NumberFormatException | ArithmeticException exception) {
            sender.sendMessage(ChatColor.RED + "Enter a valid non-negative amount with up to 2 decimal places.");
            return true;
        }

        boolean changed = switch (action) {
            case "give" -> economy.deposit(target.getUniqueId(), amountCents);
            case "take" -> economy.withdraw(target.getUniqueId(), amountCents);
            case "set" -> economy.setBalance(target.getUniqueId(), amountCents);
            default -> false;
        };
        if (!changed) {
            sender.sendMessage(ChatColor.RED + "Could not apply that change. Check the amount and the player's balance.");
            return true;
        }

        String formatted = "$" + EconomyManager.format(amountCents);
        String verb = switch (action) {
            case "give" -> "gave";
            case "take" -> "took from";
            default -> "set the balance of";
        };
        sender.sendMessage(ChatColor.GREEN + "Successfully " + verb + " " + target.getName() + ": " + formatted + ".");
        target.sendMessage(ChatColor.GOLD + "An administrator used /eco " + action + " on your balance. Amount: " + formatted);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return List.of("give", "take", "set").stream()
                    .filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }
        return List.of();
    }
}
