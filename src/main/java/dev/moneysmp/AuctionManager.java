package dev.moneysmp;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AuctionManager {
 private final JavaPlugin plugin; private final EconomyManager economy; private final File file; private final YamlConfiguration data;
 public AuctionManager(JavaPlugin plugin,EconomyManager economy){
  this.plugin=plugin;this.economy=economy;file=new File(plugin.getDataFolder(),"auctions.yml");data=YamlConfiguration.loadConfiguration(file);
  expireListings();
 }
 public static long parsePrice(String input){
  try{BigDecimal n=new BigDecimal(input);if(n.signum()<=0||n.scale()>2)return -1;return n.setScale(2,RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();}
  catch(RuntimeException ex){return -1;}
 }
 public boolean sell(Player seller,long price,int amount){
  if(price<=0||amount<=0)return false;
  int max=plugin.getConfig().getInt("auction.max-listings",5);
  if(ownedCount(seller.getUniqueId())>=max){seller.sendMessage("§cYou have reached your listing limit ("+max+").");return false;}
  ItemStack hand=seller.getInventory().getItemInMainHand();
  if(hand.getType().isAir()||hand.getAmount()<amount){seller.sendMessage("§cYou don't have that many items in your hand.");return false;}
  int id=data.getInt("next-id",1);data.set("next-id",id+1);
  ItemStack listed=hand.clone();listed.setAmount(amount);hand.setAmount(hand.getAmount()-amount);
  String p="listings."+id;
  data.set(p+".seller",seller.getUniqueId().toString());data.set(p+".seller-name",seller.getName());
  data.set(p+".price",price);data.set(p+".item",listed);data.set(p+".created",System.currentTimeMillis());
  Location loc=seller.getLocation();data.set(p+".world",loc.getWorld().getName());data.set(p+".x",loc.getX());data.set(p+".y",loc.getY());data.set(p+".z",loc.getZ());
  save();seller.sendMessage("§aListed "+amount+"x "+listed.getType().name().toLowerCase().replace('_',' ')+" as #"+id+" for $"+EconomyManager.format(price)+".");return true;
 }
 public int ownedCount(UUID owner){
  ConfigurationSection s=data.getConfigurationSection("listings");if(s==null)return 0;int n=0;
  for(String id:s.getKeys(false))if(owner.toString().equals(s.getString(id+".seller")))n++;return n;
 }
 public List<String> ownedListings(UUID owner){
  List<String> out=new ArrayList<>();ConfigurationSection s=data.getConfigurationSection("listings");if(s==null)return out;
  for(String id:s.getKeys(false))if(owner.toString().equals(s.getString(id+".seller")))out.add("#"+id+" - $"+EconomyManager.format(s.getLong(id+".price"))+" - "+itemName(s.getItemStack(id+".item")));
  return out;
 }
 public boolean cancel(Player player,int id){
  String p="listings."+id;if(!data.isConfigurationSection(p)||!player.getUniqueId().toString().equals(data.getString(p+".seller")))return false;
  ItemStack item=data.getItemStack(p+".item");data.set(p,null);save();returnItem(player,item);player.sendMessage("§aAuction #"+id+" cancelled; your item was returned.");return true;
 }
 public void open(Player player){
  AuctionHolder holder=new AuctionHolder();Inventory inv=Bukkit.createInventory(holder,54,Component.text("Auction House"));holder.setInventory(inv);
  ConfigurationSection s=data.getConfigurationSection("listings");int slot=0;
  if(s!=null)for(String key:s.getKeys(false)){
   if(slot>=45)break;ItemStack item=s.getItemStack(key+".item");if(item==null||item.getType().isAir())continue;
   ItemStack icon=item.clone();ItemMeta meta=icon.getItemMeta();
   List<Component> lore=new ArrayList<>();lore.add(Component.text("Seller: "+s.getString(key+".seller-name","Unknown")));
   lore.add(Component.text("Price: $"+EconomyManager.format(s.getLong(key+".price"))));
   lore.add(Component.text("Listing #"+key));lore.add(Component.text("Click to purchase"));
   meta.lore(lore);icon.setItemMeta(meta);inv.setItem(slot,icon);holder.map(slot,Integer.parseInt(key));slot++;
  }
  if(slot==0)inv.setItem(22,new ItemStack(Material.BARRIER));
  player.openInventory(inv);
 }
 public void buy(Player buyer,int id){
  String p="listings."+id;if(!data.isConfigurationSection(p)){buyer.sendMessage("§cThat listing is no longer available.");return;}
  UUID seller;try{seller=UUID.fromString(data.getString(p+".seller",""));}catch(IllegalArgumentException ex){buyer.sendMessage("§cInvalid seller data.");return;}
  if(seller.equals(buyer.getUniqueId())){buyer.sendMessage("§eYou cannot buy your own listing.");return;}
  ItemStack item=data.getItemStack(p+".item");long price=data.getLong(p+".price",-1);
  if(item==null||item.getType().isAir()||price<=0){buyer.sendMessage("§cThat listing is invalid.");return;}
  if(!canFit(buyer,item)){buyer.sendMessage("§cYour inventory does not have enough room.");return;}
  if(!economy.withdraw(buyer.getUniqueId(),price)){buyer.sendMessage("§cInsufficient funds. Price: $"+EconomyManager.format(price));return;}
  if(!economy.deposit(seller,price)){economy.deposit(buyer.getUniqueId(),price);buyer.sendMessage("§cSeller cannot receive payment; purchase cancelled.");return;}
  String sellerName=data.getString(p+".seller-name","A player");data.set(p,null);save();
  buyer.getInventory().addItem(item);buyer.sendMessage("§aPurchased "+item.getAmount()+"x "+itemName(item)+" for $"+EconomyManager.format(price)+".");
  Player online=Bukkit.getPlayer(seller);if(online!=null)online.sendMessage("§aYour auction #"+id+" sold for $"+EconomyManager.format(price)+".");
 }
 public void expireListings(){
  long hours=Math.max(1,plugin.getConfig().getLong("auction.duration-hours",48));long cutoff=System.currentTimeMillis()-hours*3_600_000L;
  ConfigurationSection s=data.getConfigurationSection("listings");if(s==null)return;
  for(String id:new ArrayList<>(s.getKeys(false))){
   String p="listings."+id;if(data.getLong(p+".created",System.currentTimeMillis())>cutoff)continue;
   ItemStack item=data.getItemStack(p+".item");String sellerText=data.getString(p+".seller","");
   try{Player online=Bukkit.getPlayer(UUID.fromString(sellerText));if(online!=null)returnItem(online,item);else dropAtSavedLocation(p,item);}
   catch(IllegalArgumentException ex){dropAtSavedLocation(p,item);}
   data.set(p,null);
  }save();
 }
 private void dropAtSavedLocation(String path,ItemStack item){
  if(item==null||item.getType().isAir())return;World world=Bukkit.getWorld(data.getString(path+".world",""));
  if(world==null&&!Bukkit.getWorlds().isEmpty())world=Bukkit.getWorlds().get(0);
  if(world!=null)world.dropItemNaturally(new Location(world,data.getDouble(path+".x"),data.getDouble(path+".y"),data.getDouble(path+".z")),item);
 }
 private void returnItem(Player player,ItemStack item){if(item==null||item.getType().isAir())return;for(ItemStack leftover:player.getInventory().addItem(item).values())player.getWorld().dropItemNaturally(player.getLocation(),leftover);}
 private boolean canFit(Player player,ItemStack item){
  int remaining=item.getAmount();for(ItemStack stack:player.getInventory().getStorageContents()){
   if(stack==null||stack.getType().isAir())remaining-=item.getMaxStackSize();
   else if(stack.isSimilar(item))remaining-=Math.max(0,stack.getMaxStackSize()-stack.getAmount());
   if(remaining<=0)return true;
  }return false;
 }
 private static String itemName(ItemStack item){return item==null?"item":item.getType().name().toLowerCase().replace('_',' ');}
 private void save(){try{data.save(file);}catch(IOException ex){plugin.getLogger().severe("Could not save auctions.yml: "+ex.getMessage());}}
}
