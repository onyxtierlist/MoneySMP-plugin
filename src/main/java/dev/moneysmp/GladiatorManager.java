package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class GladiatorManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;
    private final Map<UUID, UUID> requests = new HashMap<>();
    private final Map<UUID, UUID> duels = new HashMap<>();
    private final Set<UUID> returnToLobby = new HashSet<>();

    public GladiatorManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "gladiator-arena.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized boolean setLocation(String key, Location location) {
        if (location.getWorld() == null) return false;
        String path = "arena." + key;
        data.set(path + ".world", location.getWorld().getName());
        data.set(path + ".x", location.getX());
        data.set(path + ".y", location.getY());
        data.set(path + ".z", location.getZ());
        data.set(path + ".yaw", location.getYaw());
        data.set(path + ".pitch", location.getPitch());
        return save();
    }

    public synchronized Location getLocation(String key) {
        String path = "arena." + key;
        var world = Bukkit.getWorld(data.getString(path + ".world", ""));
        if (world == null || !data.contains(path + ".x")) return null;
        return new Location(world, data.getDouble(path + ".x"), data.getDouble(path + ".y"),
                data.getDouble(path + ".z"), (float) data.getDouble(path + ".yaw"),
                (float) data.getDouble(path + ".pitch"));
    }

    public synchronized boolean challenge(Player challenger, Player target) {
        if (challenger.equals(target) || isDueling(challenger.getUniqueId()) || isDueling(target.getUniqueId())) return false;
        if (getLocation("spawn1") == null || getLocation("spawn2") == null || getLocation("lobby") == null) return false;
        requests.put(target.getUniqueId(), challenger.getUniqueId());
        challenger.sendMessage("§aGladiator challenge sent to " + target.getName() + ".");
        target.sendMessage("§e" + challenger.getName() + " challenged you! Use /gladiator accept.");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            synchronized (GladiatorManager.this) {
                if (challenger.getUniqueId().equals(requests.get(target.getUniqueId()))) {
                    requests.remove(target.getUniqueId());
                    if (challenger.isOnline()) challenger.sendMessage("§7Your gladiator challenge expired.");
                    if (target.isOnline()) target.sendMessage("§7That gladiator challenge expired.");
                }
            }
        }, 1200L);
        return true;
    }

    public synchronized boolean accept(Player target) {
        UUID challengerId = requests.remove(target.getUniqueId());
        if (challengerId == null) return false;
        Player challenger = Bukkit.getPlayer(challengerId);
        if (challenger == null || !challenger.isOnline() || isDueling(challengerId) || isDueling(target.getUniqueId())) return false;
        Location first = getLocation("spawn1");
        Location second = getLocation("spawn2");
        if (first == null || second == null) return false;
        duels.put(challengerId, target.getUniqueId());
        duels.put(target.getUniqueId(), challengerId);
        challenger.teleport(first);
        target.teleport(second);
        Bukkit.broadcastMessage("§6[Gladiators] §e" + challenger.getName() + " §fvs §e" + target.getName() + " §f— fight!");
        return true;
    }

    public synchronized boolean isDueling(UUID player) { return duels.containsKey(player); }

    public synchronized boolean areOpponents(UUID first, UUID second) {
        return second.equals(duels.get(first)) && first.equals(duels.get(second));
    }

    public synchronized void finish(Player loser) {
        UUID opponentId = duels.remove(loser.getUniqueId());
        if (opponentId == null) return;
        duels.remove(opponentId);
        Player winner = Bukkit.getPlayer(opponentId);
        Bukkit.broadcastMessage("§6[Gladiators] §e" + (winner == null ? "Opponent" : winner.getName())
                + " §fwon the duel against §e" + loser.getName() + "§f!");
        returnToLobby.add(loser.getUniqueId());
        if (winner != null) {
            winner.sendMessage("§aYou won the gladiator duel!");
            Location lobby = getLocation("lobby");
            if (lobby != null) winner.teleport(lobby);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location lobby = getLocation("lobby");
            if (lobby != null && loser.isOnline()) loser.teleport(lobby);
            returnToLobby.remove(loser.getUniqueId());
        }, 1L);
    }

    public synchronized void quit(Player player) {
        requests.remove(player.getUniqueId());
        UUID opponentId = duels.get(player.getUniqueId());
        if (opponentId != null) {
            Player opponent = Bukkit.getPlayer(opponentId);
            duels.remove(player.getUniqueId());
            duels.remove(opponentId);
            if (opponent != null) {
                opponent.sendMessage("§aYou win! Your opponent left the arena.");
                Location lobby = getLocation("lobby");
                if (lobby != null) opponent.teleport(lobby);
            }
        }
    }

    public synchronized boolean leave(Player player) {
        if (!isDueling(player.getUniqueId())) return false;
        finish(player);
        return true;
    }

    public synchronized Location consumeRespawnLocation(Player player) {
        if (!returnToLobby.remove(player.getUniqueId())) return null;
        return getLocation("lobby");
    }

    private boolean save() {
        try { data.save(file); return true; }
        catch (IOException exception) {
            plugin.getLogger().severe("Could not save gladiator-arena.yml: " + exception.getMessage());
            return false;
        }
    }
}
