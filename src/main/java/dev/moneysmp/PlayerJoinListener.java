package dev.moneysmp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {
    private final EconomyManager economy;

    public PlayerJoinListener(EconomyManager economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        economy.initialize(event.getPlayer());
    }
}
