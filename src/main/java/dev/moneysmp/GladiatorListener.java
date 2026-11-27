package dev.moneysmp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;

public final class GladiatorListener implements Listener {
    private final GladiatorManager gladiators;

    public GladiatorListener(GladiatorManager gladiators) { this.gladiators = gladiators; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = event.getDamager() instanceof Player p ? p : null;
        if (gladiators.isDueling(victim.getUniqueId())) {
            if (attacker == null || !gladiators.areOpponents(victim.getUniqueId(), attacker.getUniqueId())) event.setCancelled(true);
        } else if (attacker != null && gladiators.isDueling(attacker.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (gladiators.isDueling(event.getEntity().getUniqueId())) gladiators.finish(event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        gladiators.quit(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        var location = gladiators.consumeRespawnLocation(event.getPlayer());
        if (location != null) event.setRespawnLocation(location);
    }
}
