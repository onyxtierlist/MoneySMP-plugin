package dev.moneysmp;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
public final class PlayerShopHolder implements InventoryHolder {
 private final String shopId;
 private Inventory inventory;
 public PlayerShopHolder(String shopId){this.shopId=shopId;}
 public String getShopId(){return shopId;}
 public void setInventory(Inventory inventory){this.inventory=inventory;}
 @Override public Inventory getInventory(){return inventory;}
}
