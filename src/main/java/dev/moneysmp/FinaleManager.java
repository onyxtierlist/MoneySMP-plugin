package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public final class FinaleManager {
    private final JavaPlugin plugin;
    private final SeasonPointsManager points;
    private final File file;
    private final YamlConfiguration data;
    private BukkitTask endTask;
    private long generation;
    private long endsAt;

    public FinaleManager(JavaPlugin plugin, SeasonPointsManager points) {
        this.plugin = plugin;
        this.points = points;
        this.file = new File(plugin.getDataFolder(), "finale.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized boolean start(long seconds) {
        if (seconds < 30 || seconds > 86400 || isRunning()) return false;
        generation++;
        long token = generation;
        endsAt = System.currentTimeMillis() + seconds * 1000L;
        data.set("active", true);
        data.set("started-at", Instant.now().toString());
        data.set("duration-seconds", seconds);
        data.set("starting-standings", points.leaderboard().stream()
                .map(team -> team + ":" + points.getPoints(team)).toList());
        if (!save()) {
            data.set("active", false);
            endsAt = 0;
            return false;
        }
        Bukkit.broadcastMessage("§6§lTHE SEASON FINALE HAS BEGUN!");
        Bukkit.broadcastMessage("§eFinal standings lock in " + seconds + " seconds. Good luck!");
        endTask = Bukkit.getScheduler().runTaskLater(plugin, () -> finish(token, "time limit"), seconds * 20L);
        return true;
    }

    public synchronized boolean stop() {
        if (!isRunning()) return false;
        generation++;
        if (endTask != null) endTask.cancel();
        endTask = null;
        finishNow("ended by an administrator");
        return true;
    }

    public synchronized boolean isRunning() {
        return data.getBoolean("active", false) && endsAt > System.currentTimeMillis();
    }

    public synchronized long secondsRemaining() {
        return isRunning() ? Math.max(0, (endsAt - System.currentTimeMillis() + 999) / 1000) : 0;
    }

    public synchronized String results() {
        List<String> leaderboard = points.leaderboard();
        if (leaderboard.isEmpty()) return "No teams are configured.";
        StringBuilder result = new StringBuilder("§6§lFinale standings");
        int rank = 1;
        for (String team : leaderboard) {
            result.append("\n§e#").append(rank++).append(" ").append(team)
                    .append(" §7— §b").append(points.getPoints(team)).append(" points");
        }
        return result.toString();
    }

    private synchronized void finish(long token, String reason) {
        if (token != generation || !isRunning()) return;
        finishNow(reason);
    }

    private void finishNow(String reason) {
        endsAt = 0;
        data.set("active", false);
        data.set("ended-at", Instant.now().toString());
        data.set("end-reason", reason);
        save();
        endTask = null;
        List<String> standings = points.leaderboard();
        if (standings.isEmpty()) {
            Bukkit.broadcastMessage("§6[Finale] §eThe finale has ended, but no teams are configured.");
            return;
        }
        long top = points.getPoints(standings.get(0));
        List<String> winners = standings.stream().filter(team -> points.getPoints(team) == top).toList();
        Bukkit.broadcastMessage("§6§lTHE SEASON FINALE HAS ENDED!");
        Bukkit.broadcastMessage("§eChampion" + (winners.size() == 1 ? ": " : "s: ") + "§b" + String.join("§e, §b", winners)
                + " §7with §b" + top + " season points§7.");
        Bukkit.broadcastMessage("§7Finale " + reason + ". Use /finale results to view the full table.");
    }

    private boolean save() {
        try {
            data.save(file);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save finale.yml: " + exception.getMessage());
            return false;
        }
    }
}
