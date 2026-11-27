package dev.moneysmp;
import org.bukkit.entity.Player;import org.bukkit.event.EventHandler;import org.bukkit.event.Listener;import org.bukkit.event.inventory.InventoryClickEvent;import org.bukkit.event.inventory.InventoryDragEvent;import org.bukkit.inventory.Inventory;
public final class AuctionListener implements Listener{
 private final AuctionManager auctions;public AuctionListener(AuctionManager auctions){this.auctions=auctions;}
 @EventHandler public void click(InventoryClickEvent e){Inventory top=e.getView().getTopInventory();if(!(top.getHolder() instanceof AuctionHolder h))return;e.setCancelled(true);if(e.getClickedInventory()!=top||!(e.getWhoClicked() instanceof Player p))return;Integer id=h.listingAt(e.getSlot());if(id!=null){auctions.buy(p,id);p.closeInventory();}}
 @EventHandler public void drag(InventoryDragEvent e){if(e.getView().getTopInventory().getHolder() instanceof AuctionHolder)e.setCancelled(true);}
}
