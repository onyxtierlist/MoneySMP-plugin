package dev.moneysmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SeasonPointsManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;
    private final TeamManager teams;

    public SeasonPointsManager(JavaPlugin plugin, TeamManager teams) {
        this.plugin = plugin;
        this.teams = teams;
        this.file = new File(plugin.getDataFolder(), "season-points.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        for (String team : teams.getTeams()) data.addDefault("points." + team, 0L);
        data.options().copyDefaults(true);
        save();
    }

    public synchronized long getPoints(String team) {
        String canonical = canonical(team);
        return canonical == null ? 0L : Math.max(0L, data.getLong("points." + canonical));
    }

    public synchronized boolean award(String team, long points) {
        String canonical = canonical(team);
        if (canonical == null || points <= 0 || getPoints(canonical) > Long.MAX_VALUE - points) return false;
        data.set("points." + canonical, getPoints(canonical) + points);
        return save();
    }

    public synchronized boolean set(String team, long points) {
        String canonical = canonical(team);
        if (canonical == null || points < 0) return false;
        data.set("points." + canonical, points);
        return save();
    }

    public synchronized boolean reset() {
        for (String team : teams.getTeams()) data.set("points." + team, 0L);
        return save();
    }

    public synchronized List<String> leaderboard() {
        return teams.getTeams().stream()
                .sorted(Comparator.comparingLong(this::getPoints).reversed())
                .toList();
    }

    private String canonical(String name) {
        return teams.getTeams().stream().filter(t -> t.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private boolean save() {
        try {
            data.save(file);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save season-points.yml: " + exception.getMessage());
            return false;
        }
    }
}
