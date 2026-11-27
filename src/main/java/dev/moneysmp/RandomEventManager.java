package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RandomEventManager implements Listener {
    public enum EventType { SLIPPERY, HEALTHY, ANONYMOUS, DOUBLE_DOUGH, ULTRA_HARDCORE, SHIELDLESS, SHOPPING_SPREE }
    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private EventType active;
    private int taskId = -1;
    private final Set<UUID> eventParticipants = new HashSet<>();

    public RandomEventManager(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public synchronized boolean start(EventType event, int durationSeconds) {
        if (active != null || durationSeconds < 10) return false;
        active = event;
        eventParticipants.clear();
        eventParticipants.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList());
        Bukkit.broadcastMessage("§d[Random Event] §e" + event.name().replace('_', ' ') + " §fhas started!");
        if (event == EventType.SHOPPING_SPREE) {
            for (Player player : Bukkit.getOnlinePlayers()) economy.deposit(player.getUniqueId(), 1000L);
            Bukkit.broadcastMessage("§aEveryone online received $10.00 for the shopping spree!");
        }
        if (event == EventType.DOUBLE_DOUGH) {
            economy.setRewardMultiplier(2);
            Bukkit.broadcastMessage("§eDouble Dough is active: job rewards and daily rewards are doubled.");
        }
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0L, 40L);
        Bukkit.getScheduler().runTaskLater(plugin, this::stop, durationSeconds * 20L);
        return true;
    }

    public synchronized void stop() {
        if (active == null) return;
        EventType ended = active;
        active = null;
        economy.setRewardMultiplier(1);
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
        for (UUID id : eventParticipants) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.REGENERATION);
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            }
        }
        eventParticipants.clear();
        Bukkit.broadcastMessage("§d[Random Event] §f" + ended.name().replace('_', ' ') + " has ended.");
    }

    public synchronized String getStatus() {
        return active == null ? "No random event is active." : active.name().replace('_', ' ');
    }

    private synchronized void tick() {
        if (active == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            switch (active) {
                case SLIPPERY -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, 0, true, false));
                }
                case HEALTHY -> player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, true, false));
                case ANONYMOUS -> player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0, true, false));
                case DOUBLE_DOUGH, ULTRA_HARDCORE, SHIELDLESS, SHOPPING_SPREE -> { }
            }
        }
    }

    @EventHandler
    public void onNaturalRegeneration(EntityRegainHealthEvent event) {
        if (active == EventType.ULTRA_HARDCORE && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) event.setCancelled(true);
    }

    @EventHandler
    public void onShieldUse(PlayerInteractEvent event) {
        if (active != EventType.SHIELDLESS) return;
        if (event.getItem() != null && event.getItem().getType() == Material.SHIELD) event.setCancelled(true);
        if (event.getPlayer().getInventory().getItemInOffHand().getType() == Material.SHIELD) event.setCancelled(true);
    }
}
