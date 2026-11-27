package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public final class BaltopCommand implements CommandExecutor {
    private final EconomyManager economy;

    public BaltopCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var accounts = economy.getKnownBalances().entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .toList();
        if (accounts.isEmpty()) {
            sender.sendMessage("§eNo player balances have been recorded yet.");
            return true;
        }
        sender.sendMessage("§6§lMoneySMP — Richest Players");
        int rank = 1;
        for (Map.Entry<UUID, Long> account : accounts) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(account.getKey());
            String name = player.getName() == null ? account.getKey().toString().substring(0, 8) : player.getName();
            sender.sendMessage("§e#" + rank++ + " §f" + name + " §7— §a$" + EconomyManager.format(account.getValue()));
        }
        return true;
    }
}
