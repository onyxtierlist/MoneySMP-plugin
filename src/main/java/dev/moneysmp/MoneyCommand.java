package dev.moneysmp;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class MoneyCommand implements CommandExecutor, TabCompleter {
    private final EconomyManager economy;

    public MoneyCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (!player.hasPermission("moneysmp.balance")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }

        long balance = economy.getBalanceCents(player.getUniqueId());
        player.sendMessage(ChatColor.GOLD + "Balance: " + ChatColor.GREEN
                + "$" + EconomyManager.format(balance));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        return List.of();
    }
}
