package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public final class GladiatorCommand implements CommandExecutor, TabCompleter {
    private final GladiatorManager gladiators;

    public GladiatorCommand(GladiatorManager gladiators) { this.gladiators = gladiators; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6Gladiators: /gladiator challenge <player>, accept, leave");
            sender.sendMessage("§7Admin setup: /gladiator set <lobby|spawn1|spawn2>");
            return true;
        }
        if (args[0].equalsIgnoreCase("set") && args.length == 2) {
            if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
            if (!(sender instanceof Player player)) { sender.sendMessage("Only players can set arena locations."); return true; }
            String key = args[1].toLowerCase();
            if (!List.of("lobby", "spawn1", "spawn2").contains(key)) {
                sender.sendMessage("§cChoose lobby, spawn1, or spawn2."); return true;
            }
            sender.sendMessage(gladiators.setLocation(key, player.getLocation())
                    ? "§aSaved gladiator " + key + " location." : "§cCould not save location.");
            return true;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can duel."); return true; }
        if (args[0].equalsIgnoreCase("challenge") && args.length == 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { player.sendMessage("§cThat player is not online."); return true; }
            if (!gladiators.challenge(player, target)) player.sendMessage("§cChallenge unavailable. Check arena setup and duel status.");
            return true;
        }
        if (args[0].equalsIgnoreCase("accept") && args.length == 1) {
            if (!gladiators.accept(player)) player.sendMessage("§cNo valid challenge was found, or the arena is unavailable.");
            return true;
        }
        if (args[0].equalsIgnoreCase("leave") && args.length == 1) {
            if (!gladiators.leave(player)) player.sendMessage("§cYou are not in a gladiator duel.");
            return true;
        }
        player.sendMessage("§cUsage: /gladiator <challenge <player>|accept|leave|set <lobby|spawn1|spawn2>>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("challenge", "accept", "leave", "set");
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) return List.of("lobby", "spawn1", "spawn2");
        if (args.length == 2 && args[0].equalsIgnoreCase("challenge")) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        return List.of();
    }
}
