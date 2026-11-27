package dev.moneysmp;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public final class ShopManager {
 private final JavaPlugin plugin;
 private final EconomyManager economy;
 public ShopManager(JavaPlugin plugin,EconomyManager economy){
  this.plugin=plugin; this.economy=economy;
  if(!plugin.getConfig().isConfigurationSection("shop.items")){
   plugin.getConfig().set("shop.items.DIAMOND",250.00);
   plugin.getConfig().set("shop.items.IRON_INGOT",25.00);
   plugin.getConfig().set("shop.items.GOLD_INGOT",50.00);
   plugin.getConfig().set("shop.items.EMERALD",100.00);
   plugin.saveConfig();
  }
 }
 public void open(Player player){
  ShopMenuHolder holder=new ShopMenuHolder();
  Inventory menu=Bukkit.createInventory(holder,27,Component.text("MoneySMP Shop"));
  holder.setInventory(menu);
  ConfigurationSection section=plugin.getConfig().getConfigurationSection("shop.items");
  if(section!=null){
   int slot=0;
   for(String key:section.getKeys(false)){
    if(slot>=menu.getSize()) break;
    Material type=Material.matchMaterial(key);
    if(type==null||!type.isItem()){plugin.getLogger().warning("Invalid shop material: "+key);continue;}
    long price=getPriceCents(type);
    if(price<=0){plugin.getLogger().warning("Invalid shop price for: "+key);continue;}
    ItemStack icon=new ItemStack(type);
    ItemMeta meta=icon.getItemMeta();
    meta.displayName(Component.text("$"+EconomyManager.format(price)+" - "+pretty(type)));
    List<Component> lore=new ArrayList<>();
    lore.add(Component.text("Click to buy 1"));
    lore.add(Component.text("Price: $"+EconomyManager.format(price)));
    meta.lore(lore); icon.setItemMeta(meta);
    menu.setItem(slot++,icon);
   }
  }
  player.openInventory(menu);
 }
 public long getPriceCents(Material type){
  String path="shop.items."+type.name();
  if(!plugin.getConfig().contains(path)) return -1;
  try{return BigDecimal.valueOf(plugin.getConfig().getDouble(path)).setScale(2,RoundingMode.HALF_UP).movePointRight(2).longValueExact();}
  catch(RuntimeException ex){return -1;}
 }
 public void purchase(Player player,Material type){
  long price=getPriceCents(type);
  if(price<=0){player.sendMessage("§cThat item is not available in the shop.");return;}
  ItemStack item=new ItemStack(type);
  if(!canFitOne(player,item)){player.sendMessage("§cYour inventory is full.");return;}
  if(!economy.withdraw(player.getUniqueId(),price)){player.sendMessage("§cYou need $"+EconomyManager.format(price)+" to buy that.");return;}
  player.getInventory().addItem(item);
  player.sendMessage("§aPurchased 1 "+pretty(type)+" for $"+EconomyManager.format(price)+".");
 }
 private boolean canFitOne(Player player,ItemStack item){
  for(ItemStack content:player.getInventory().getStorageContents()){
   if(content==null||content.getType().isAir()||(content.isSimilar(item)&&content.getAmount()<content.getMaxStackSize())) return true;
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
}
