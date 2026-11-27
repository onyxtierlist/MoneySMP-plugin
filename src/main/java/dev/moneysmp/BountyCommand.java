package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class BountyCommand implements CommandExecutor, TabCompleter {
    private final BountyManager bounties;

    public BountyCommand(BountyManager bounties) {
        this.bounties = bounties;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            List<String> entries = bounties.list();
            sender.sendMessage("§6Active bounties (" + entries.size() + "):");
            if (entries.isEmpty()) sender.sendMessage("§7No active bounties.");
            else entries.forEach(sender::sendMessage);
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can place or cancel bounties.");
            return true;
        }
        if (args[0].equalsIgnoreCase("place") && args.length == 3) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                player.sendMessage("§cThe target must be online.");
                return true;
            }
            long amount = parseCents(args[2]);
            if (amount <= 0L) {
                player.sendMessage("§cEnter a valid positive amount with up to two decimals.");
                return true;
            }
            bounties.place(player, target, amount);
        } else if (args[0].equalsIgnoreCase("cancel") && args.length == 2) {
            try {
                if (!bounties.cancel(player, Integer.parseInt(args[1]))) {
                    player.sendMessage("§cYou cannot cancel that bounty.");
                }
            } catch (NumberFormatException exception) {
                player.sendMessage("§cBounty ID must be a number.");
            }
        } else {
            player.sendMessage("§e/bounty place <player> <amount> §7- Place a bounty");
            player.sendMessage("§e/bounty list §7- View active bounties | §e/bounty cancel <id> §7- Cancel your bounty");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("place", "list", "cancel").stream()
                .filter(value -> value.startsWith(args[0].toLowerCase())).toList();
        if (args.length == 2 && args[0].equalsIgnoreCase("place")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
            return names.stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }

    private static long parseCents(String value) {
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2).longValueExact();
        } catch (RuntimeException exception) {
            return -1L;
        }
    }
}
