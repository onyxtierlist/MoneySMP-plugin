package dev.moneysmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
public final class ShopListener implements Listener {
 private final ShopManager shop;
 public ShopListener(ShopManager shop){this.shop=shop;}
 @EventHandler public void onClick(InventoryClickEvent event){
  Inventory top=event.getView().getTopInventory();
  if(!(top.getHolder() instanceof ShopMenuHolder))return;
  // Cancel every inventory click while the shop is open to prevent shift-click,
  // number-key, and double-click interactions from moving display items.
  event.setCancelled(true);
  if(event.getClickedInventory()!=top)return;
  ItemStack clicked=event.getCurrentItem();
  if(clicked==null||clicked.getType().isAir()||!(event.getWhoClicked() instanceof Player player))return;
  shop.purchase(player,clicked.getType());
 }
 @EventHandler public void onDrag(InventoryDragEvent event){
  Inventory top=event.getView().getTopInventory();
  if(!(top.getHolder() instanceof ShopMenuHolder))return;
  for(int slot:event.getRawSlots())if(slot<top.getSize()){event.setCancelled(true);return;}
 }
}
