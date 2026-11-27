package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class RandomEventManager implements Listener {
    public enum EventType {
        SLIPPERY, HEALTHY, ANONYMOUS, DOUBLE_DOUGH, MINERS_HAVEN,
        EXHAUSTED, FORBIDDEN_FRUIT, VULNERABLE, ULTRA_HARDCORE,
        SHIELDLESS, SHOPPING_SPREE, STUBBY_LEGS
    }

    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final Map<UUID, Double> originalMaxHealth = new HashMap<>();
    private final Set<UUID> eventParticipants = new HashSet<>();
    private EventType active;
    private int taskId = -1;

    public RandomEventManager(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
        if (plugin.getConfig().getBoolean("random-events.automatic", false)) {
            long interval = Math.max(5, plugin.getConfig().getLong("random-events.interval-minutes", 120));
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::startRandom,
                    interval * 1200L, interval * 1200L);
        }
    }

    public synchronized boolean start(EventType event, int durationSeconds) {
        if (active != null || durationSeconds < 10) return false;
        active = event;
        eventParticipants.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            eventParticipants.add(player.getUniqueId());
            applyImmediateEffect(player);
        }
        Bukkit.broadcastMessage("§d[Random Event] §e" + event.name().replace('_', ' ') + " §fhas started!");
        if (event == EventType.DOUBLE_DOUGH) {
            economy.setRewardMultiplier(2);
            Bukkit.broadcastMessage("§eBounty payouts are doubled while Double Dough is active.");
        }
        if (event == EventType.SHOPPING_SPREE) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 24000, 0, true, true));
            }
            Bukkit.broadcastMessage("§aEveryone online received Hero of the Village for 20 minutes!");
        }
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0L, 40L);
        Bukkit.getScheduler().runTaskLater(plugin, this::stop, durationSeconds * 20L);
        return true;
    }

    public synchronized boolean startRandom() {
        EventType[] values = EventType.values();
        EventType selected = values[new Random().nextInt(values.length)];
        int duration = Math.max(10, plugin.getConfig().getInt("random-events.duration-seconds", 120));
        return start(selected, duration);
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
            if (player != null) clearPlayerEffects(player);
        }
        for (Map.Entry<UUID, Double> entry : new HashMap<>(originalMaxHealth).entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) restoreMaxHealth(player, entry.getValue());
        }
        originalMaxHealth.clear();
        eventParticipants.clear();
        Bukkit.broadcastMessage("§d[Random Event] §f" + ended.name().replace('_', ' ') + " has ended.");
    }

    public synchronized String getStatus() {
        return active == null ? "No random event is active." : active.name().replace('_', ' ');
    }

    public synchronized List<String> getEventNames() {
        return Arrays.stream(EventType.values()).map(e -> e.name().toLowerCase(Locale.ROOT)).toList();
    }

    private synchronized void tick() {
        if (active == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            eventParticipants.add(player.getUniqueId());
            applyImmediateEffect(player);
        }
    }

    private void applyImmediateEffect(Player player) {
        switch (active) {
            case HEALTHY -> setMaxHealth(player, 26.0);
            case VULNERABLE -> setMaxHealth(player, 15.0);
            case SHOPPING_SPREE -> player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 24000, 0, true, true));
            case SLIPPERY -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, 0, true, false));
            }
            case ANONYMOUS -> player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0, true, false));
            case EXHAUSTED -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0, true, false));
            case DOUBLE_DOUGH, MINERS_HAVEN, FORBIDDEN_FRUIT, ULTRA_HARDCORE, SHIELDLESS, STUBBY_LEGS -> { }
        }
    }

    private void setMaxHealth(Player player, double value) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;
        originalMaxHealth.putIfAbsent(player.getUniqueId(), attribute.getBaseValue());
        attribute.setBaseValue(value);
        if (player.getHealth() > value) player.setHealth(value);
    }

    private void restoreMaxHealth(Player player, double original) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;
        attribute.setBaseValue(original);
        if (player.getHealth() > original) player.setHealth(original);
    }

    private void clearPlayerEffects(Player player) {
        for (PotionEffectType type : List.of(PotionEffectType.SPEED, PotionEffectType.REGENERATION,
                PotionEffectType.INVISIBILITY, PotionEffectType.JUMP_BOOST, PotionEffectType.SLOWNESS,
                PotionEffectType.HERO_OF_THE_VILLAGE)) player.removePotionEffect(type);
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

    @EventHandler
    public void onBlockDrops(BlockDropItemEvent event) {
        if (active == EventType.MINERS_HAVEN && JobsManager.isOre(event.getBlockState().getType())) {
            for (Item item : event.getItems()) {
                ItemStack stack = item.getItemStack();
                stack.setAmount(Math.min(stack.getMaxStackSize(), stack.getAmount() * 2));
            }
        }
        if (active == EventType.FORBIDDEN_FRUIT && event.getBlockState().getType().name().endsWith("_LEAVES")) {
            for (Item item : event.getItems()) {
                ItemStack stack = item.getItemStack();
                if (stack.getType() == Material.APPLE) {
                    stack.setType(Material.GOLDEN_APPLE);
                    stack.setAmount(3);
                }
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (active == EventType.STUBBY_LEGS && event.getEntity() instanceof Player
                && event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setDamage(event.getDamage() * 2.0);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (active != null) {
            eventParticipants.add(event.getPlayer().getUniqueId());
            applyImmediateEffect(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Double original = originalMaxHealth.remove(event.getPlayer().getUniqueId());
        if (original != null) restoreMaxHealth(event.getPlayer(), original);
    }
}
