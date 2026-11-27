package dev.moneysmp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class BountyListener implements Listener {
    private final BountyManager bounties;

    public BountyListener(BountyManager bounties) {
        this.bounties = bounties;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) bounties.claim(killer, event.getEntity());
    }
}
