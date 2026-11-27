package dev.moneysmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DailyCommand implements CommandExecutor {
    private final DailyRewardManager rewards;

    public DailyCommand(DailyRewardManager rewards) {
        this.rewards = rewards;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can claim daily rewards.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("claim")) {
            rewards.claim(player);
        } else if (args[0].equalsIgnoreCase("status")) {
            rewards.status(player);
        } else {
            player.sendMessage("§e/daily claim §7- Claim your reward | §e/daily status §7- Check the cooldown");
        }
        return true;
    }
}
