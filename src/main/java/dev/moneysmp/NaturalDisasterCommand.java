package dev.moneysmp;

import org.bukkit.command.*;
import java.util.List;
import java.util.Locale;

public final class NaturalDisasterCommand implements CommandExecutor, TabCompleter {
    private final NaturalDisasterManager disasters;

    public NaturalDisasterCommand(NaturalDisasterManager disasters) { this.disasters = disasters; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("§6" + disasters.getStatus());
            return true;
        }
        if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
        if (args[0].equalsIgnoreCase("stop")) {
            disasters.stop();
            sender.sendMessage("§aDisaster stop requested.");
            return true;
        }
        if (args[0].equalsIgnoreCase("start") && args.length >= 2 && args.length <= 3) {
            NaturalDisasterManager.Disaster disaster;
            try { disaster = NaturalDisasterManager.Disaster.valueOf(args[1].toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException exception) {
                sender.sendMessage("§cChoose storm, earthquake, heatwave, or blizzard."); return true;
            }
            int duration = 120;
            if (args.length == 3) {
                try { duration = Integer.parseInt(args[2]); }
                catch (NumberFormatException exception) { sender.sendMessage("§cDuration must be seconds."); return true; }
            }
            if (duration < 10 || duration > 1800) { sender.sendMessage("§cDuration must be between 10 and 1800 seconds."); return true; }
            if (!disasters.start(disaster, duration)) sender.sendMessage("§cA disaster is already active.");
            else sender.sendMessage("§aStarted " + disaster.name().toLowerCase(Locale.ROOT) + " for " + duration + " seconds.");
            return true;
        }
        sender.sendMessage("§cUsage: /disaster <status|start <storm|earthquake|heatwave|blizzard> [seconds]|stop>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("status", "start", "stop");
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) return List.of("storm", "earthquake", "heatwave", "blizzard");
        return List.of();
    }
}
