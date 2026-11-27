package dev.moneysmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class TierManager {
    public static final List<String> TIERS = List.of("S", "A", "B", "C", "D", "F");
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;

    public TierManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "tiers.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized String getTier(UUID player) {
        return data.getString(player.toString(), "Unranked");
    }

    public synchronized boolean setTier(UUID player, String tier) {
        if (!tier.equalsIgnoreCase("unranked") && TIERS.stream().noneMatch(t -> t.equalsIgnoreCase(tier))) return false;
        data.set(player.toString(), tier.equalsIgnoreCase("unranked") ? null : tier.toUpperCase(Locale.ROOT));
        try { data.save(file); return true; }
        catch (IOException exception) {
            plugin.getLogger().severe("Could not save tiers.yml: " + exception.getMessage());
            return false;
        }
    }

    public synchronized Map<String, List<UUID>> getRankings() {
        Map<String, List<UUID>> result = new LinkedHashMap<>();
        for (String tier : TIERS) result.put(tier, new ArrayList<>());
        for (String key : data.getKeys(false)) {
            try {
                String tier = data.getString(key);
                if (result.containsKey(tier)) result.get(tier).add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) { }
        }
        return result;
    }
}
