package dev.moneysmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public final class EconomyManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;
    private final long startingBalanceCents;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "balances.yml");

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create plugin data folder.");
        }

        this.data = YamlConfiguration.loadConfiguration(file);
        BigDecimal startingBalance = BigDecimal.valueOf(
                plugin.getConfig().getDouble("starting-balance", 100.00));
        this.startingBalanceCents = toCents(startingBalance);
        if (startingBalanceCents < 0) {
            throw new IllegalArgumentException("starting-balance cannot be negative.");
        }
    }

    public synchronized void initialize(Player player) {
        String path = player.getUniqueId().toString();
        if (!data.contains(path)) {
            data.set(path, startingBalanceCents);
            save();
        }
    }

    public synchronized long getBalanceCents(UUID uuid) {
        String path = uuid.toString();
        if (!data.contains(path)) {
            data.set(path, startingBalanceCents);
            save();
        }
        return Math.max(0L, data.getLong(path, startingBalanceCents));
    }

    public synchronized void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save balances.yml: " + exception.getMessage());
        }
    }

    private static long toCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2).longValueExact();
    }

    public static String format(long cents) {
        return BigDecimal.valueOf(cents, 2).setScale(2).toPlainString();
    }
}
