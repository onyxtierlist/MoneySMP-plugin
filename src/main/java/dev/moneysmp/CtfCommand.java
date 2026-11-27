package dev.moneysmp;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public final class CtfCommand implements CommandExecutor, TabCompleter {
    private final CtfManager ctf;
    private final TeamManager teams;

    public CtfCommand(CtfManager ctf, TeamManager teams) {
        this.ctf = ctf;
        this.teams = teams;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("§6[CTF] §f" + ctf.getStatus());
            return true;
        }
        if (args[0].equalsIgnoreCase("set") && args.length == 3) {
            if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
            if (!(sender instanceof Player player)) { sender.sendMessage("Only players can set CTF locations."); return true; }
            if (!ctf.setLocation(args[1], args[2], player.getLocation())) {
                sender.sendMessage("§cUse /ctf set <team> <flag|base> with a valid team.");
            } else sender.sendMessage("§aSaved " + args[2] + " location for " + args[1] + ".");
            return true;
        }
        if (args[0].equalsIgnoreCase("start") && args.length == 1) {
            if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
            sender.sendMessage(ctf.start() ? "§aCTF match started." : "§cMatch already active or fewer than two teams have flag and base locations.");
            return true;
        }
        if (args[0].equalsIgnoreCase("stop") && args.length == 1) {
            if (!sender.hasPermission("moneysmp.admin")) { sender.sendMessage("§cYou lack permission."); return true; }
            ctf.stop("The match was stopped by an administrator.");
            sender.sendMessage("§aCTF stop requested.");
            return true;
        }
        sender.sendMessage("§cUsage: /ctf <status|set <team> <flag|base>|start|stop>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("status", "set", "start", "stop");
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) return teams.getTeams();
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) return List.of("flag", "base");
        return List.of();
    }
}
