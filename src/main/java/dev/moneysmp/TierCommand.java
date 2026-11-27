package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class TierCommand implements CommandExecutor, TabCompleter {
    private final TierManager tiers;

    public TierCommand(TierManager tiers) { this.tiers = tiers; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) { sender.sendMessage("Use /tier list or /tier set."); return true; }
            sender.sendMessage("§6Your tier: §e" + tiers.getTier(player.getUniqueId()));
            return true;
        }
        if (args[0].equalsIgnoreCase("list") && args.length == 1) {
            sender.sendMessage("§6§lMoneySMP Tier Rankings");
            tiers.getRankings().forEach((tier, players) -> {
                String names = players.stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName)
                        .map(name -> name == null ? "Unknown" : name).reduce((a,b) -> a + ", " + b).orElse("—");
                sender.sendMessage("§e" + tier + "§7: §f" + names);
            });
            return true;
        }
        if (args[0].equalsIgnoreCase("set") && args.length == 3) {
            if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            String tier = args[2].toUpperCase(Locale.ROOT);
            if (!tiers.setTier(target.getUniqueId(), tier)) {
                sender.sendMessage("§cTier must be S, A, B, C, D, F, or unranked.");
                return true;
            }
            sender.sendMessage("§aSet " + (target.getName() == null ? args[1] : target.getName()) + "'s tier to " + tier + ".");
            if (target.isOnline()) target.getPlayer().sendMessage("§6Your tier is now §e" + tier + "§6.");
            return true;
        }
        sender.sendMessage("§cUsage: /tier [list] or /tier set <player> <S|A|B|C|D|F|unranked>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("list", "set");
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) return List.of("S", "A", "B", "C", "D", "F", "unranked");
        return List.of();
    }
}
