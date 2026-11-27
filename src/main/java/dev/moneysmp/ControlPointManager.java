package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ControlPointManager {
    private final JavaPlugin plugin;
    private final TeamManager teams;
    private final File file;
    private final YamlConfiguration data;
    private final Map<String, Integer> captureProgress = new HashMap<>();
    private final Map<String, String> capturingTeam = new HashMap<>();
    private final Map<String, Integer> rewardTicks = new HashMap<>();
    private final int captureSeconds;
    private final int radius;
    private final int rewardCents;
    private final int rewardIntervalSeconds;

    public ControlPointManager(JavaPlugin plugin, TeamManager teams) {
        this.plugin = plugin;
        this.teams = teams;
        this.file = new File(plugin.getDataFolder(), "control-points.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        this.captureSeconds = Math.max(5, plugin.getConfig().getInt("control-points.capture-seconds", 30));
        this.radius = Math.max(2, plugin.getConfig().getInt("control-points.radius", 6));
        this.rewardCents = Math.max(0, plugin.getConfig().getInt("control-points.reward-cents", 2500));
        this.rewardIntervalSeconds = Math.max(30, plugin.getConfig().getInt("control-points.reward-interval-seconds", 300));
    }

    public synchronized boolean setPoint(String name, Location location) {
        if (name == null || name.isBlank() || location.getWorld() == null) return false;
        String path = "points." + name.toLowerCase(Locale.ROOT);
        data.set(path + ".name", name);
        data.set(path + ".world", location.getWorld().getName());
        data.set(path + ".x", location.getBlockX() + 0.5);
        data.set(path + ".y", location.getBlockY());
        data.set(path + ".z", location.getBlockZ() + 0.5);
        data.set(path + ".owner", null);
        captureProgress.remove(name.toLowerCase(Locale.ROOT));
        capturingTeam.remove(name.toLowerCase(Locale.ROOT));
        return save();
    }

    public synchronized List<String> getPointNames() {
        ConfigurationSection section = data.getConfigurationSection("points");
        if (section == null) return List.of();
        return new ArrayList<>(section.getKeys(false));
    }

    public synchronized String getOwner(String name) {
        return data.getString("points." + name.toLowerCase(Locale.ROOT) + ".owner");
    }

    public synchronized Location getLocation(String name) {
        String path = "points." + name.toLowerCase(Locale.ROOT);
        World world = Bukkit.getWorld(data.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, data.getDouble(path + ".x"), data.getDouble(path + ".y"), data.getDouble(path + ".z"));
    }

    public synchronized String status(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        if (!getPointNames().contains(key)) return null;
        String owner = getOwner(key);
        String progressTeam = capturingTeam.get(key);
        int progress = captureProgress.getOrDefault(key, 0);
        return "Owner: " + (owner == null ? "Neutral" : owner)
                + (progressTeam == null ? "" : " | Capturing: " + progressTeam + " (" + progress + "/" + captureSeconds + "s)");
    }

    public synchronized void tick() {
        for (String key : getPointNames()) {
            Location center = getLocation(key);
            if (center == null || center.getWorld() == null) continue;
            Set<String> presentTeams = new HashSet<>();
            for (Player player : center.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(center) > radius * radius) continue;
                String team = teams.getTeam(player.getUniqueId());
                if (team != null) presentTeams.add(team);
            }

            if (presentTeams.size() != 1) {
                captureProgress.remove(key);
                capturingTeam.remove(key);
            } else {
                String team = presentTeams.iterator().next();
                String owner = getOwner(key);
                if (!team.equalsIgnoreCase(owner == null ? "" : owner)) {
                    if (!team.equals(capturingTeam.get(key))) {
                        capturingTeam.put(key, team);
                        captureProgress.put(key, 0);
                    }
                    int progress = captureProgress.merge(key, 1, Integer::sum);
                    if (progress >= captureSeconds) {
                        data.set("points." + key + ".owner", team);
                        save();
                        captureProgress.remove(key);
                        capturingTeam.remove(key);
                        Bukkit.broadcastMessage("§6[Control Points] §e" + team + " §fhas captured §b" + data.getString("points." + key + ".name", key) + "§f!");
                    }
                } else {
                    captureProgress.remove(key);
                    capturingTeam.remove(key);
                }
            }

            String owner = getOwner(key);
            if (owner != null && rewardCents > 0) {
                int ticks = rewardTicks.merge(key, 1, Integer::sum);
                if (ticks >= rewardIntervalSeconds) {
                    rewardTicks.put(key, 0);
                    teams.deposit(owner, rewardCents);
                    Bukkit.broadcastMessage("§6[Control Points] §e" + owner + " §fteam earned $" + EconomyManager.format(rewardCents) + " from " + data.getString("points." + key + ".name", key) + ".");
                }
            }
        }
    }

    private boolean save() {
        try { data.save(file); return true; }
        catch (IOException exception) {
            plugin.getLogger().severe("Could not save control-points.yml: " + exception.getMessage());
            return false;
        }
    }
}
