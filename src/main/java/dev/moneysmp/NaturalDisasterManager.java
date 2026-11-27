package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class NaturalDisasterManager {
    public enum Disaster { STORM, EARTHQUAKE, HEATWAVE, BLIZZARD }
    private final JavaPlugin plugin;
    private Disaster active;
    private int remainingSeconds;
    private int taskId = -1;
    private final Map<World, boolean[]> weatherBefore = new HashMap<>();

    public NaturalDisasterManager(JavaPlugin plugin) { this.plugin = plugin; }

    public synchronized boolean start(Disaster disaster, int durationSeconds) {
        if (active != null || durationSeconds < 10) return false;
        active = disaster;
        remainingSeconds = durationSeconds;
        weatherBefore.clear();
        for (World world : Bukkit.getWorlds()) {
            weatherBefore.put(world, new boolean[]{world.hasStorm(), world.isThundering()});
            if (disaster == Disaster.STORM) {
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(durationSeconds * 20);
            }
        }
        Bukkit.broadcastMessage("§c[Natural Disaster] §e" + disaster.name().replace('_', ' ') + " §fhas begun!");
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0L, 100L);
        return true;
    }

    public synchronized void stop() {
        if (active == null) return;
        Disaster ended = active;
        active = null;
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
        weatherBefore.forEach((world, weather) -> {
            world.setStorm(weather[0]);
            world.setThundering(weather[1]);
        });
        weatherBefore.clear();
        Bukkit.broadcastMessage("§a[Natural Disaster] §f" + ended.name().replace('_', ' ') + " has ended.");
    }

    public synchronized String getStatus() {
        return active == null ? "No disaster is active." : active.name() + " — " + remainingSeconds + " seconds remaining";
    }

    private synchronized void tick() {
        if (active == null) return;
        remainingSeconds -= 5;
        for (Player player : Bukkit.getOnlinePlayers()) {
            switch (active) {
                case STORM -> {
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 2, 0), 5, 1, 0.5, 1, 0.01);
                    if (player.getLocation().getBlock().getLightFromSky() > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0, true, false));
                }
                case EARTHQUAKE -> {
                    player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 12, 0.8, 0.2, 0.8, 0, player.getLocation().getBlock().getBlockData());
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0, true, false));
                }
                case HEATWAVE -> {
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 4, 0.5, 0.5, 0.5, 0.01);
                    if (!player.getLocation().getBlock().isLiquid()) player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0, true, false));
                }
                case BLIZZARD -> {
                    player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 2, 0), 18, 1.5, 1, 1.5, 0.02);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0, true, false));
                }
            }
        }
        if (remainingSeconds <= 0) stop();
    }
}
