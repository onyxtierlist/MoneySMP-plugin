package dev.moneysmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class TeamManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;
    private final List<String> teams;

    public TeamManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "teams.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        this.teams = plugin.getConfig().getStringList("teams.names");
        if (teams.isEmpty()) teams.addAll(List.of("Red", "Blue", "Green", "Yellow", "Purple", "Orange"));
        for (String team : teams) {
            data.addDefault("teams." + team + ".members", new ArrayList<>());
            data.addDefault("teams." + team + ".balance", 0L);
        }
        data.options().copyDefaults(true);
        save();
    }

    public synchronized List<String> getTeams() { return List.copyOf(teams); }

    public synchronized String getTeam(UUID player) {
        String team = data.getString("players." + player);
        return team != null && teams.stream().anyMatch(t -> t.equalsIgnoreCase(team)) ? team : null;
    }

    public synchronized boolean join(UUID player, String requested) {
        String team = findTeam(requested);
        if (team == null || getTeam(player) != null) return false;
        List<String> members = data.getStringList("teams." + team + ".members");
        if (members.contains(player.toString())) return false;
        members.add(player.toString());
        data.set("teams." + team + ".members", members);
        data.set("players." + player, team);
        if (data.getString("teams." + team + ".leader") == null) data.set("teams." + team + ".leader", player.toString());
        save();
        return true;
    }

    public synchronized boolean leave(UUID player) {
        String team = getTeam(player);
        if (team == null) return false;
        List<String> members = data.getStringList("teams." + team + ".members");
        members.remove(player.toString());
        data.set("teams." + team + ".members", members);
        data.set("players." + player, null);
        if (player.toString().equals(data.getString("teams." + team + ".leader"))) {
            data.set("teams." + team + ".leader", members.isEmpty() ? null : members.get(0));
        }
        save();
        return true;
    }

    public synchronized List<UUID> getMembers(String team) {
        String canonical = findTeam(team);
        if (canonical == null) return List.of();
        List<UUID> result = new ArrayList<>();
        for (String raw : data.getStringList("teams." + canonical + ".members")) {
            try { result.add(UUID.fromString(raw)); } catch (IllegalArgumentException ignored) { }
        }
        return result;
    }

    public synchronized UUID getLeader(String team) {
        String canonical = findTeam(team);
        if (canonical == null) return null;
        String raw = data.getString("teams." + canonical + ".leader");
        try { return raw == null ? null : UUID.fromString(raw); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    public synchronized long getBalance(String team) {
        String canonical = findTeam(team);
        return canonical == null ? 0L : Math.max(0L, data.getLong("teams." + canonical + ".balance"));
    }

    public synchronized boolean deposit(String team, long cents) {
        String canonical = findTeam(team);
        if (canonical == null || cents <= 0) return false;
        long balance = getBalance(canonical);
        if (balance > Long.MAX_VALUE - cents) return false;
        data.set("teams." + canonical + ".balance", balance + cents);
        return save();
    }

    public synchronized boolean withdraw(String team, long cents) {
        String canonical = findTeam(team);
        if (canonical == null || cents <= 0 || getBalance(canonical) < cents) return false;
        data.set("teams." + canonical + ".balance", getBalance(canonical) - cents);
        return save();
    }

    private String findTeam(String requested) {
        return teams.stream().filter(t -> t.equalsIgnoreCase(requested)).findFirst().orElse(null);
    }

    private boolean save() {
        try { data.save(file); return true; }
        catch (IOException exception) {
            plugin.getLogger().severe("Could not save teams.yml: " + exception.getMessage());
            return false;
        }
    }
}
