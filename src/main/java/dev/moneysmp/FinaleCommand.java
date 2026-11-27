package dev.moneysmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;

public final class FinaleCommand implements CommandExecutor, TabCompleter {
    private final FinaleManager finale;

    public FinaleCommand(FinaleManager finale) {
        this.finale = finale;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            if (finale.isRunning()) sender.sendMessage("§6The season finale is LIVE! §e" + finale.secondsRemaining() + " seconds remaining.");
            else sender.sendMessage("§7The season finale is not currently running.");
            return true;
        }
        if (args[0].equalsIgnoreCase("results") && args.length == 1) {
            sender.sendMessage(finale.results());
            return true;
        }
        if (!sender.hasPermission("moneysmp.admin")) {
            sender.sendMessage("§cYou do not have permission to manage the finale.");
            return true;
        }
        if (args[0].equalsIgnoreCase("start") && (args.length == 1 || args.length == 2)) {
            long seconds = 1800;
            if (args.length == 2) {
                try { seconds = Long.parseLong(args[1]); }
                catch (NumberFormatException exception) {
                    sender.sendMessage("§cDuration must be a whole number of seconds.");
                    return true;
                }
            }
            if (finale.start(seconds)) sender.sendMessage("§aSeason finale started for " + seconds + " seconds.");
            else sender.sendMessage("§cCould not start finale. Is one already running? Duration must be 30–86400 seconds.");
            return true;
        }
        if (args[0].equalsIgnoreCase("stop") && args.length == 1) {
            if (finale.stop()) sender.sendMessage("§aSeason finale ended.");
            else sender.sendMessage("§cNo finale is currently running.");
            return true;
        }
        sender.sendMessage("§eUsage: /finale <status|results|start [seconds]|stop>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("status", "results", "start", "stop").stream()
                .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) return List.of("600", "1800", "3600");
        return List.of();
    }
}
