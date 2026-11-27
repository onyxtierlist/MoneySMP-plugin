package dev.moneysmp;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
public final class PlayerShopListener implements Listener {
 private final PlayerShopManager shops;
 public PlayerShopListener(PlayerShopManager shops){this.shops=shops;}
 @EventHandler public void onInteract(PlayerInteractEvent event){
  if(event.getAction()!=Action.RIGHT_CLICK_BLOCK)return;
  Block block=event.getClickedBlock();
  if(block==null||!shops.isShop(block))return;
  Player player=event.getPlayer();
  if(shops.isOwner(block,player.getUniqueId()))return;
  event.setCancelled(true);
  shops.open(block,player);
 }
 @EventHandler public void onBreak(BlockBreakEvent event){
  Block block=event.getBlock();
  if(!shops.isShop(block))return;
  if(shops.isOwner(block,event.getPlayer().getUniqueId())){
   event.setCancelled(true);
   event.getPlayer().sendMessage("§eRemove your shop with /pshop remove before breaking this chest.");
  }else{
   event.setCancelled(true);
   event.getPlayer().sendMessage("§cYou cannot break another player's shop chest.");
  }
 }
 @EventHandler public void onClick(InventoryClickEvent event){
  Inventory top=event.getView().getTopInventory();
  if(!(top.getHolder() instanceof PlayerShopHolder holder))return;
  event.setCancelled(true);
  if(event.getClickedInventory()!=top)return;
  ItemStack clicked=event.getCurrentItem();
  if(clicked==null||clicked.getType().isAir()||!(event.getWhoClicked() instanceof Player player))return;
  shops.buy(player,holder.getShopId(),clicked);
  player.closeInventory();
 }
 @EventHandler public void onDrag(InventoryDragEvent event){
  Inventory top=event.getView().getTopInventory();
  if(top.getHolder() instanceof PlayerShopHolder)event.setCancelled(true);
 }
}
