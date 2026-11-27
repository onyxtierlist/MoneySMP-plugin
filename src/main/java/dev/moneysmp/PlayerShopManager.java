package dev.moneysmp;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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
import java.util.Locale;
import java.util.UUID;

public final class PlayerShopManager {
 private final JavaPlugin plugin;
 private final EconomyManager economy;
 private final File file;
 private final YamlConfiguration data;

 public PlayerShopManager(JavaPlugin plugin, EconomyManager economy) {
  this.plugin=plugin; this.economy=economy;
  this.file=new File(plugin.getDataFolder(),"player-shops.yml");
  this.data=YamlConfiguration.loadConfiguration(file);
 }

 public static String id(Block block) {
  return block.getWorld().getName()+","+block.getX()+","+block.getY()+","+block.getZ();
 }
 public boolean isShop(Block block) { return data.isConfigurationSection("shops."+id(block)); }
 public boolean isOwner(Block block, UUID player) {
  return isShop(block) && player.toString().equals(data.getString("shops."+id(block)+".owner"));
 }
 public boolean create(Block block, Player owner, long initialPrice) {
  if (!(block.getState() instanceof Chest) || initialPrice<=0 || isShop(block)) return false;
  String path="shops."+id(block);
  data.set(path+".owner",owner.getUniqueId().toString());
  data.set(path+".world",block.getWorld().getName());
  data.set(path+".x",block.getX()); data.set(path+".y",block.getY()); data.set(path+".z",block.getZ());
  data.set(path+".prices."+owner.getInventory().getItemInMainHand().getType().name(),initialPrice);
  save();
  return true;
 }
 public boolean remove(Block block, Player owner) {
  if(!isOwner(block,owner.getUniqueId())) return false;
  data.set("shops."+id(block),null); save(); return true;
 }
 public boolean setPrice(Block block, Player owner, Material material, long price) {
  if(!isOwner(block,owner.getUniqueId()) || material==Material.AIR || price<=0) return false;
  data.set("shops."+id(block)+".prices."+material.name(),price); save(); return true;
 }
 public List<String> listOwned(UUID owner) {
  List<String> result=new ArrayList<>();
  ConfigurationSection shops=data.getConfigurationSection("shops");
  if(shops==null)return result;
  for(String key:shops.getKeys(false)) {
   if(owner.toString().equals(shops.getString(key+".owner"))) result.add(key.replace(',',' '));
  }
  return result;
 }
 public void open(Block block, Player buyer) {
  if(!isShop(block) || !(block.getState() instanceof Chest chest)) return;
  String shopId=id(block);
  PlayerShopHolder holder=new PlayerShopHolder(shopId);
  Inventory menu=Bukkit.createInventory(holder,54,Component.text("Player Shop"));
  holder.setInventory(menu);
  ItemStack[] stock=chest.getBlockInventory().getStorageContents();
  int slot=0;
  for(ItemStack stack:stock) {
   if(stack==null || stack.getType().isAir()) continue;
   long price=priceFor(shopId,stack.getType());
   ItemStack display=stack.clone();
   ItemMeta meta=display.getItemMeta();
   List<Component> lore=new ArrayList<>();
   if(price>0) {
    lore.add(Component.text("Price per item: $"+EconomyManager.format(price)));
    lore.add(Component.text("Click to buy 1"));
   } else {
    lore.add(Component.text("This item is not for sale"));
   }
   meta.lore(lore); display.setItemMeta(meta);
   if(slot<menu.getSize()) menu.setItem(slot++,display);
  }
  if(slot==0) menu.setItem(22,new ItemStack(Material.BARRIER));
  buyer.openInventory(menu);
 }
 public void buy(Player buyer, String shopId, ItemStack display) {
  ConfigurationSection shop=data.getConfigurationSection("shops."+shopId);
  if(shop==null) {buyer.sendMessage("§cThis shop no longer exists.");return;}
  String worldName=shop.getString("world");
  if(worldName==null || Bukkit.getWorld(worldName)==null){buyer.sendMessage("§cThis shop's world is unavailable.");return;}
  Block block=Bukkit.getWorld(worldName).getBlockAt(shop.getInt("x"),shop.getInt("y"),shop.getInt("z"));
  if(!(block.getState() instanceof Chest chest)){buyer.sendMessage("§cThe shop chest is missing.");return;}
  Material type=display.getType();
  long price=priceFor(shopId,type);
  if(price<=0){buyer.sendMessage("§cThat item is not for sale.");return;}
  Inventory stock=chest.getBlockInventory();
  int stockSlot=-1;
  ItemStack stocked=null;
  for(int i=0;i<stock.getSize();i++){
   ItemStack candidate=stock.getItem(i);
   if(candidate!=null && candidate.getType()==type && candidate.getAmount()>0){stockSlot=i;stocked=candidate;break;}
  }
  if(stockSlot<0){buyer.sendMessage("§cThis item is out of stock.");return;}
  ItemStack purchased=stocked.clone(); purchased.setAmount(1);
  if(!canFit(buyer,purchased)){buyer.sendMessage("§cYour inventory is full.");return;}
  UUID owner;
  try{owner=UUID.fromString(shop.getString("owner",""));}catch(IllegalArgumentException ex){buyer.sendMessage("§cThis shop has invalid ownership data.");return;}
  if(owner.equals(buyer.getUniqueId())){buyer.sendMessage("§eYou cannot buy from your own shop.");return;}
  if(!economy.withdraw(buyer.getUniqueId(),price)){buyer.sendMessage("§cYou need $"+EconomyManager.format(price)+" to buy that.");return;}
  if(!economy.deposit(owner,price)){
   economy.deposit(buyer.getUniqueId(),price);
   buyer.sendMessage("§cThe shop owner cannot receive this payment right now.");
   return;
  }
  stocked.setAmount(stocked.getAmount()-1);
  if(stocked.getAmount()<=0)stock.setItem(stockSlot,null);
  chest.update(true,false);
  buyer.getInventory().addItem(purchased);
  Player online=Bukkit.getPlayer(owner);
  if(online!=null)online.sendMessage("§aYour shop sold 1 "+pretty(type)+" for $"+EconomyManager.format(price)+".");
  buyer.sendMessage("§aBought 1 "+pretty(type)+" for $"+EconomyManager.format(price)+".");
 }
 private long priceFor(String shopId,Material type){return data.getLong("shops."+shopId+".prices."+type.name(),-1L);}
 private boolean canFit(Player player,ItemStack item){
  for(ItemStack content:player.getInventory().getStorageContents()){
   if(content==null||content.getType().isAir()||(content.isSimilar(item)&&content.getAmount()<content.getMaxStackSize()))return true;
  }
  return false;
 }
 private static String pretty(Material type){
  StringBuilder out=new StringBuilder();
  for(String word:type.name().toLowerCase(Locale.ROOT).split("_")){
   if(out.length()>0)out.append(' ');
   out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
  }
  return out.toString();
 }
 private void save(){
  try{data.save(file);}catch(IOException ex){plugin.getLogger().severe("Could not save player-shops.yml: "+ex.getMessage());}
 }
 public static long parsePrice(String input){
  try{
   BigDecimal amount=new BigDecimal(input);
   if(amount.scale()>2 || amount.signum()<=0)return -1;
   return amount.setScale(2,RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
  }catch(RuntimeException ex){return -1;}
 }
}
