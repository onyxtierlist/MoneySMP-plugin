package dev.moneysmp;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.HashMap;
import java.util.Map;
public final class AuctionHolder implements InventoryHolder {
 private Inventory inventory;
 private final Map<Integer,Integer> listingIds=new HashMap<>();
 public void setInventory(Inventory inventory){this.inventory=inventory;}
 public void map(int slot,int id){listingIds.put(slot,id);}
 public Integer listingAt(int slot){return listingIds.get(slot);}
 @Override public Inventory getInventory(){return inventory;}
}
