package dev.moneysmp;
import org.bukkit.entity.Player;import org.bukkit.event.EventHandler;import org.bukkit.event.Listener;import org.bukkit.event.player.PlayerQuitEvent;
public final class TradeListener implements Listener{
 private final TradeManager trades;public TradeListener(TradeManager trades){this.trades=trades;}
 @EventHandler public void quit(PlayerQuitEvent e){trades.quit(e.getPlayer());}
}
