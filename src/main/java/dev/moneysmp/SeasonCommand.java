package dev.moneysmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class SeasonCommand implements CommandExecutor, TabCompleter {
    private final SeasonPointsManager points;
    private final TeamManager teams;

    public SeasonCommand(SeasonPointsManager points, TeamManager teams) {
        this.points = points;
        this.teams = teams;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("top")) {
            sender.sendMessage("§6§lSeason Team Standings");
            int rank = 1;
            for (String team : points.leaderboard()) {
                sender.sendMessage("§e#" + rank++ + " " + team + " §7— §b" + points.getPoints(team) + " points");
            }
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("points") && args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console: use /season top.");
                return true;
            }
            String team = teams.getTeam(player.getUniqueId());
            if (team == null) sender.sendMessage("§cYou are not in a team.");
            else sender.sendMessage("§e" + team + " has §b" + points.getPoints(team) + " season points§e.");
            return true;
        }
        if (!sender.hasPermission("moneysmp.admin")) {
            sender.sendMessage("§cYou do not have permission to manage season points.");
            return true;
        }
        if (sub.equals("award") && args.length == 3) {
            Long amount = parseNonNegative(args[2]);
            if (amount == null || amount == 0) {
                sender.sendMessage("§cPoints must be a positive whole number.");
            } else if (points.award(args[1], amount)) {
                sender.sendMessage("§aAwarded " + amount + " season points to " + args[1] + ".");
            } else sender.sendMessage("§cUnknown team, invalid amount, or save failure.");
            return true;
        }
        if (sub.equals("set") && args.length == 3) {
            Long amount = parseNonNegative(args[2]);
            if (amount == null) sender.sendMessage("§cPoints must be a non-negative whole number.");
            else if (points.set(args[1], amount)) sender.sendMessage("§aSet " + args[1] + " to " + amount + " season points.");
            else sender.sendMessage("§cUnknown team or save failure.");
            return true;
        }
        if (sub.equals("reset") && args.length == 1) {
            if (points.reset()) sender.sendMessage("§aAll season team points have been reset.");
            else sender.sendMessage("§cCould not save the season reset.");
            return true;
        }
        sender.sendMessage("§eUsage: /season <top|points|award <team> <points>|set <team> <points>|reset>");
        return true;
    }

    private Long parseNonNegative(String raw) {
        try {
            long value = Long.parseLong(raw);
            return value < 0 ? null : value;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("top", "points", "award", "set", "reset");
        if (args.length == 2 && (args[0].equalsIgnoreCase("award") || args[0].equalsIgnoreCase("set"))) {
            return teams.getTeams().stream().filter(t -> t.toLowerCase(Locale.ROOT)
                    .startsWith(args[1].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        return List.of();
    }
}
