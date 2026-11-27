package dev.moneysmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class BankManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;

    public BankManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bank.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized long getBalance(UUID player) {
        return Math.max(0L, data.getLong(player.toString(), 0L));
    }

    public synchronized boolean deposit(UUID player, long cents) {
        if (cents <= 0 || getBalance(player) > Long.MAX_VALUE - cents) return false;
        data.set(player.toString(), getBalance(player) + cents);
        return save();
    }

    public synchronized boolean withdraw(UUID player, long cents) {
        long balance = getBalance(player);
        if (cents <= 0 || balance < cents) return false;
        data.set(player.toString(), balance - cents);
        return save();
    }

    private boolean save() {
        try {
            data.save(file);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save bank.yml: " + exception.getMessage());
            return false;
        }
    }
}
