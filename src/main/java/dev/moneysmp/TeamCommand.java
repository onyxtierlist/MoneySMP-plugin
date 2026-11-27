package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class TeamCommand implements CommandExecutor, TabCompleter {
    private final TeamManager teams;
    private final EconomyManager economy;

    public TeamCommand(TeamManager teams, EconomyManager economy) {
        this.teams = teams;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§6§lMoneySMP Teams");
            for (String team : teams.getTeams()) {
                UUID leader = teams.getLeader(team);
                OfflinePlayer owner = leader == null ? null : Bukkit.getOfflinePlayer(leader);
                sender.sendMessage("§e" + team + " §7— §f" + teams.getMembers(team).size()
                        + " members §7| §a$" + EconomyManager.format(teams.getBalance(team))
                        + (owner == null ? "" : " §7| Leader: §f" + (owner.getName() == null ? "Unknown" : owner.getName())));
            }
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use that team action.");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("join") && args.length == 2) {
            if (teams.join(player.getUniqueId(), args[1])) player.sendMessage("§aYou joined team " + args[1] + ".");
            else player.sendMessage("§cYou are already in a team, or that team does not exist.");
            return true;
        }
        if (sub.equals("leave") && args.length == 1) {
            if (teams.leave(player.getUniqueId())) player.sendMessage("§eYou left your team.");
            else player.sendMessage("§cYou are not in a team.");
            return true;
        }
        if (sub.equals("info") && args.length <= 2) {
            String team = args.length == 2 ? args[1] : teams.getTeam(player.getUniqueId());
            if (team == null || teams.getTeams().stream().noneMatch(t -> t.equalsIgnoreCase(team))) {
                player.sendMessage("§cSpecify a valid team or join one first.");
                return true;
            }
            player.sendMessage("§6Team " + team + " §7| §f" + teams.getMembers(team).size()
                    + " members §7| Treasury: §a$" + EconomyManager.format(teams.getBalance(team)));
            return true;
        }
        if ((sub.equals("deposit") || sub.equals("withdraw")) && args.length == 2) {
            String team = teams.getTeam(player.getUniqueId());
            if (team == null) { player.sendMessage("§cJoin a team first."); return true; }
            long cents;
            try {
                BigDecimal amount = new BigDecimal(args[1]);
                if (amount.signum() <= 0 || amount.scale() > 2) throw new NumberFormatException();
                cents = amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
            } catch (ArithmeticException | NumberFormatException ex) {
                player.sendMessage("§cEnter a positive amount with up to two decimal places.");
                return true;
            }
            if (sub.equals("deposit")) {
                if (economy.getBalanceCents(player.getUniqueId()) < cents) {
                    player.sendMessage("§cNot enough money in your wallet."); return true;
                }
                if (!economy.withdraw(player.getUniqueId(), cents)) {
                    player.sendMessage("§cCould not withdraw from wallet."); return true;
                }
                if (!teams.deposit(team, cents)) {
                    economy.deposit(player.getUniqueId(), cents);
                    player.sendMessage("§cTeam treasury deposit failed; your wallet was refunded."); return true;
                }
                player.sendMessage("§aDeposited $" + EconomyManager.format(cents) + " into " + team + "'s treasury.");
            } else {
                if (!player.getUniqueId().equals(teams.getLeader(team))) {
                    player.sendMessage("§cOnly the team leader can withdraw from the treasury."); return true;
                }
                if (!teams.withdraw(team, cents)) { player.sendMessage("§cThe team treasury has insufficient funds."); return true; }
                if (!economy.deposit(player.getUniqueId(), cents)) {
                    teams.deposit(team, cents);
                    player.sendMessage("§cWallet credit failed; treasury was refunded."); return true;
                }
                player.sendMessage("§aWithdrew $" + EconomyManager.format(cents) + " from the team treasury.");
            }
            return true;
        }
        player.sendMessage("§cUsage: /team <list|join <team>|leave|info [team]|deposit <amount>|withdraw <amount>>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("list", "join", "leave", "info", "deposit", "withdraw");
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) return teams.getTeams().stream()
                .filter(t -> t.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) return teams.getTeams();
        return List.of();
    }
}
