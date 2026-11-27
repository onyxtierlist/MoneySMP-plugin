package dev.moneysmp;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public final class ControlPointCommand implements CommandExecutor, TabCompleter {
    private final ControlPointManager points;

    public ControlPointCommand(ControlPointManager points) { this.points = points; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§6§lControl Points");
            if (points.getPointNames().isEmpty()) sender.sendMessage("§7No points configured. Admin: /controlpoint set <name>");
            for (String name : points.getPointNames()) sender.sendMessage("§e" + name + " §7— §f" + points.status(name));
            return true;
        }
        if (args[0].equalsIgnoreCase("set") && args.length == 2) {
            if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
            if (!(sender instanceof Player player)) { sender.sendMessage("Only players can set a point location."); return true; }
            if (points.setPoint(args[1], player.getLocation())) sender.sendMessage("§aControl point '" + args[1] + "' set at your location.");
            else sender.sendMessage("§cCould not save that control point.");
            return true;
        }
        if (args[0].equalsIgnoreCase("status") && args.length == 2) {
            String status = points.status(args[1]);
            sender.sendMessage(status == null ? "§cUnknown control point." : "§6" + args[1] + " §7— §f" + status);
            return true;
        }
        sender.sendMessage("§cUsage: /controlpoint <list|set <name>|status <name>>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("list", "set", "status");
        if (args.length == 2 && args[0].equalsIgnoreCase("status")) return points.getPointNames();
        return List.of();
    }
}
