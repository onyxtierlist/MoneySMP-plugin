package dev.moneysmp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CtfListener implements Listener {
    private final CtfManager ctf;

    public CtfListener(CtfManager ctf) { this.ctf = ctf; }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) { ctf.playerDiedOrQuit(event.getEntity()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { ctf.playerDiedOrQuit(event.getPlayer()); }
}
