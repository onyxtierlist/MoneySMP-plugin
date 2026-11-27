package dev.moneysmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class JobCommand implements CommandExecutor, TabCompleter {
    private final JobsManager jobs;

    public JobCommand(JobsManager jobs) {
        this.jobs = jobs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use jobs.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            jobs.info(player);
        } else if (args[0].equalsIgnoreCase("join") && args.length == 2) {
            jobs.join(player, args[1]);
        } else if (args[0].equalsIgnoreCase("leave")) {
            jobs.leave(player);
        } else {
            player.sendMessage("§e/job join <miner|farmer|hunter|fisher> §7- Choose a job");
            player.sendMessage("§e/job leave §7- Leave your job | §e/job info §7- View your job");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("join", "leave", "info").stream()
                .filter(value -> value.startsWith(args[0].toLowerCase())).toList();
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return List.of("miner", "farmer", "hunter", "fisher").stream()
                    .filter(value -> value.startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}
