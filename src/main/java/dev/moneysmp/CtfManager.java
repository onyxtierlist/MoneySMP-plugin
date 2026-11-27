package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class CtfManager {
    private final JavaPlugin plugin;
    private final TeamManager teams;
    private final File file;
    private final YamlConfiguration data;
    private final Map<String, UUID> flagCarriers = new HashMap<>();
    private final Map<UUID, String> carriedFlags = new HashMap<>();
    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private boolean running;
    private int taskId = -1;
    private final int winScore;

    public CtfManager(JavaPlugin plugin, TeamManager teams) {
        this.plugin = plugin;
        this.teams = teams;
        this.file = new File(plugin.getDataFolder(), "ctf.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        this.winScore = Math.max(1, plugin.getConfig().getInt("ctf.score-to-win", 3));
    }

    public synchronized boolean setLocation(String team, String type, Location location) {
        String canonical = canonicalTeam(team);
        if (canonical == null || location.getWorld() == null || !(type.equalsIgnoreCase("flag") || type.equalsIgnoreCase("base"))) return false;
        String path = "teams." + canonical + "." + type.toLowerCase(Locale.ROOT);
        data.set(path + ".world", location.getWorld().getName());
        data.set(path + ".x", location.getX());
        data.set(path + ".y", location.getY());
        data.set(path + ".z", location.getZ());
        return save();
    }

    public synchronized Location getLocation(String team, String type) {
        String canonical = canonicalTeam(team);
        if (canonical == null) return null;
        String path = "teams." + canonical + "." + type;
        World world = Bukkit.getWorld(data.getString(path + ".world", ""));
        if (world == null || !data.contains(path + ".x")) return null;
        return new Location(world, data.getDouble(path + ".x"), data.getDouble(path + ".y"), data.getDouble(path + ".z"));
    }

    public synchronized boolean start() {
        if (running) return false;
        List<String> ready = teams.getTeams().stream()
                .filter(team -> getLocation(team, "flag") != null && getLocation(team, "base") != null).toList();
        if (ready.size() < 2) return false;
        running = true;
        scores.clear();
        ready.forEach(team -> scores.put(team, 0));
        flagCarriers.clear();
        carriedFlags.clear();
        Bukkit.broadcastMessage("§6[Capture the Flag] §eThe match has started! Capture enemy flags and return them to your base.");
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 5L, 5L);
        return true;
    }

    public synchronized void stop(String reason) {
        if (!running) return;
        running = false;
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
        flagCarriers.clear();
        carriedFlags.clear();
        Bukkit.broadcastMessage("§6[Capture the Flag] §f" + reason);
    }

    public synchronized boolean isRunning() { return running; }

    public synchronized void playerDiedOrQuit(Player player) {
        String flag = carriedFlags.remove(player.getUniqueId());
        if (flag != null) {
            flagCarriers.remove(flag);
            Bukkit.broadcastMessage("§e" + player.getName() + " dropped the " + flag + " flag. It has returned to base.");
        }
    }

    public synchronized String getStatus() {
        if (!running) return "No CTF match is active.";
        return scores.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue() + "/" + winScore)
                .reduce((a, b) -> a + " | " + b).orElse("Match active");
    }

    private synchronized void tick() {
        if (!running) return;
        for (String flagTeam : scores.keySet()) {
            if (flagCarriers.containsKey(flagTeam)) continue;
            Location flag = getLocation(flagTeam, "flag");
            if (flag == null) continue;
            for (Player player : Bukkit.getOnlinePlayers()) {
                String playerTeam = teams.getTeam(player.getUniqueId());
                if (playerTeam == null || playerTeam.equalsIgnoreCase(flagTeam) || carriedFlags.containsKey(player.getUniqueId())) continue;
                if (player.getWorld().equals(flag.getWorld()) && player.getLocation().distanceSquared(flag) <= 6.25) {
                    flagCarriers.put(flagTeam, player.getUniqueId());
                    carriedFlags.put(player.getUniqueId(), flagTeam);
                    player.sendMessage("§aYou picked up the §e" + flagTeam + " §aflag! Return it to your team's base.");
                    Bukkit.broadcastMessage("§6[Capture the Flag] §e" + player.getName() + " §fhas taken the " + flagTeam + " flag!");
                    break;
                }
            }
        }

        for (Map.Entry<UUID, String> entry : new HashMap<>(carriedFlags).entrySet()) {
            Player carrier = Bukkit.getPlayer(entry.getKey());
            if (carrier == null) {
                flagCarriers.remove(entry.getValue());
                carriedFlags.remove(entry.getKey());
                continue;
            }
            String team = teams.getTeam(carrier.getUniqueId());
            if (team == null) { playerDiedOrQuit(carrier); continue; }
            Location base = getLocation(team, "base");
            if (base == null || !carrier.getWorld().equals(base.getWorld()) || carrier.getLocation().distanceSquared(base) > 6.25) continue;
            if (flagCarriers.containsKey(team)) {
                carrier.sendMessage("§cYour team's flag must be at base before you can score.");
                continue;
            }
            String capturedFlag = entry.getValue();
            carriedFlags.remove(carrier.getUniqueId());
            flagCarriers.remove(capturedFlag);
            int score = scores.merge(team, 1, Integer::sum);
            Bukkit.broadcastMessage("§6[Capture the Flag] §e" + team + " §fscored! §a" + score + "/" + winScore);
            if (score >= winScore) {
                Bukkit.broadcastMessage("§6[Capture the Flag] §e" + team + " §fteam wins the match!");
                stop(team + " team won the match.");
                return;
            }
        }
    }

    private String canonicalTeam(String requested) {
        return teams.getTeams().stream().filter(t -> t.equalsIgnoreCase(requested)).findFirst().orElse(null);
    }

    private boolean save() {
        try { data.save(file); return true; }
        catch (IOException exception) {
            plugin.getLogger().severe("Could not save ctf.yml: " + exception.getMessage());
            return false;
        }
    }
}
