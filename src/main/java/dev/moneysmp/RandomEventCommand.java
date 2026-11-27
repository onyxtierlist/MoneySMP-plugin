package dev.moneysmp;

import org.bukkit.command.*;
import java.util.List;
import java.util.Locale;

public final class RandomEventCommand implements CommandExecutor, TabCompleter {
    private final RandomEventManager events;

    public RandomEventCommand(RandomEventManager events) { this.events = events; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("§dRandom event: §f" + events.getStatus());
            return true;
        }
        if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
        if (args[0].equalsIgnoreCase("stop")) {
            events.stop();
            sender.sendMessage("§aRandom event stop requested.");
            return true;
        }
        if (args[0].equalsIgnoreCase("start") && args.length >= 2 && args.length <= 3) {
            RandomEventManager.EventType type;
            try { type = RandomEventManager.EventType.valueOf(args[1].toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException exception) {
                sender.sendMessage("§cUnknown event. Use /randomevent list."); return true;
            }
            int duration = 120;
            if (args.length == 3) {
                try { duration = Integer.parseInt(args[2]); }
                catch (NumberFormatException exception) { sender.sendMessage("§cDuration must be seconds."); return true; }
            }
            if (duration < 10 || duration > 1800) { sender.sendMessage("§cDuration must be between 10 and 1800 seconds."); return true; }
            if (!events.start(type, duration)) sender.sendMessage("§cAn event is already active.");
            else sender.sendMessage("§aStarted " + type.name().toLowerCase(Locale.ROOT) + ".");
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§dEvents: §f" + String.join(", ", events.getEventNames()));
            return true;
        }
        sender.sendMessage("§cUsage: /randomevent <status|list|start <event> [seconds]|stop>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("status", "list", "start", "stop");
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) return events.getEventNames();
        return List.of();
    }
}
