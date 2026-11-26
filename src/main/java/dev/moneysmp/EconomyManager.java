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
        ensureAccount(player.getUniqueId());
    }

    public synchronized long getBalanceCents(UUID uuid) {
        ensureAccount(uuid);
        return Math.max(0L, data.getLong(uuid.toString(), startingBalanceCents));
    }

    public synchronized boolean setBalance(UUID uuid, long amountCents) {
        if (amountCents < 0) return false;
        ensureAccount(uuid);
        data.set(uuid.toString(), amountCents);
        save();
        return true;
    }

    public synchronized boolean deposit(UUID uuid, long amountCents) {
        if (amountCents <= 0) return false;
        long current = getBalanceCents(uuid);
        if (current > Long.MAX_VALUE - amountCents) return false;
        data.set(uuid.toString(), current + amountCents);
        save();
        return true;
    }

    public synchronized boolean withdraw(UUID uuid, long amountCents) {
        if (amountCents <= 0) return false;
        long current = getBalanceCents(uuid);
        if (current < amountCents) return false;
        data.set(uuid.toString(), current - amountCents);
        save();
        return true;
    }

    /**
     * Transfers money atomically. Returns false when the sender cannot afford it
     * or the recipient balance would overflow a long.
     */
    public synchronized boolean transfer(UUID sender, UUID recipient, long amountCents) {
        if (amountCents <= 0 || sender.equals(recipient)) {
            return false;
        }

        ensureAccount(sender);
        ensureAccount(recipient);

        String senderPath = sender.toString();
        String recipientPath = recipient.toString();
        long senderBalance = Math.max(0L, data.getLong(senderPath, startingBalanceCents));
        long recipientBalance = Math.max(0L, data.getLong(recipientPath, startingBalanceCents));

        if (senderBalance < amountCents || recipientBalance > Long.MAX_VALUE - amountCents) {
            return false;
        }

        data.set(senderPath, senderBalance - amountCents);
        data.set(recipientPath, recipientBalance + amountCents);
        save();
        return true;
    }

    private void ensureAccount(UUID uuid) {
        String path = uuid.toString();
        if (!data.contains(path)) {
            data.set(path, startingBalanceCents);
            save();
        }
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
