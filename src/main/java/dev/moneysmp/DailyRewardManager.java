package dev.moneysmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public final class DailyRewardManager {
    private static final long COOLDOWN_MILLIS = 24L * 60L * 60L * 1000L;
    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final File file;
    private final YamlConfiguration data;
    private final long rewardCents;

    public DailyRewardManager(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.file = new File(plugin.getDataFolder(), "daily-rewards.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        this.rewardCents = parseConfiguredReward(plugin.getConfig().getString("daily-reward", "50.00"));
    }

    public void claim(Player player) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = data.getLong(id.toString(), 0L);
        long remaining = COOLDOWN_MILLIS - (now - last);
        if (remaining > 0L) {
            player.sendMessage("§eYour next daily reward is ready in §6" + formatDuration(remaining) + "§e.");
            return;
        }
        if (rewardCents <= 0L) {
            player.sendMessage("§cThe daily reward is not configured correctly. Contact an administrator.");
            return;
        }
        if (!economy.deposit(id, rewardCents)) {
            player.sendMessage("§cCould not deposit your daily reward. Please contact an administrator.");
            return;
        }
        data.set(id.toString(), now);
        save();
        player.sendMessage("§aDaily reward claimed: §6$" + EconomyManager.format(rewardCents) + "§a!");
    }

    public void status(Player player) {
        long last = data.getLong(player.getUniqueId().toString(), 0L);
        long remaining = COOLDOWN_MILLIS - (System.currentTimeMillis() - last);
        if (remaining <= 0L) {
            player.sendMessage("§aYour daily reward is ready! Use §e/daily claim§a.");
        } else {
            player.sendMessage("§eNext daily reward: §6" + formatDuration(remaining) + "§e.");
        }
    }

    private static long parseConfiguredReward(String value) {
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2).longValueExact();
        } catch (RuntimeException exception) {
            return -1L;
        }
    }

    private static String formatDuration(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return hours + "h " + minutes + "m " + secs + "s";
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save daily-rewards.yml: " + exception.getMessage());
        }
    }
}
