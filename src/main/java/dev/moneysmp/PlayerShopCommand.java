package dev.moneysmp;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public final class PlayerShopCommand implements CommandExecutor, TabCompleter {
 private final PlayerShopManager shops;
 public PlayerShopCommand(PlayerShopManager shops){this.shops=shops;}
 @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
  if(!(sender instanceof Player player)){sender.sendMessage("Only players can manage player shops.");return true;}
  if(args.length==0){help(player);return true;}
  String sub=args[0].toLowerCase();
  if(sub.equals("list")){
   List<String> owned=shops.listOwned(player.getUniqueId());
   player.sendMessage("§6Your shops ("+owned.size()+"): "+(owned.isEmpty()?"none":String.join(" | ",owned)));
   return true;
  }
  Block target=player.getTargetBlockExact(6);
  if(target==null || target.getType()!=Material.CHEST){
   player.sendMessage("§cLook directly at a chest within 6 blocks.");return true;
  }
  switch(sub){
   case "create" -> {
    if(args.length!=2){player.sendMessage("§eUsage: /pshop create <price>");return true;}
    Material held=player.getInventory().getItemInMainHand().getType();
    if(held.isAir()){player.sendMessage("§cHold the item you want to sell first.");return true;}
    long price=PlayerShopManager.parsePrice(args[1]);
    if(price<=0){player.sendMessage("§cEnter a valid price greater than $0.00 (max 2 decimals).");return true;}
    if(shops.create(target,player,price))player.sendMessage("§aPlayer shop created. Stock the chest; held "+held.name().toLowerCase().replace('_',' ')+" sells for $"+EconomyManager.format(price)+".");
    else player.sendMessage("§cThat chest is already a shop or cannot be used.");
   }
   case "price" -> {
    if(args.length!=2){player.sendMessage("§eUsage: /pshop price <price> (hold the item)");return true;}
    Material held=player.getInventory().getItemInMainHand().getType();
    long price=PlayerShopManager.parsePrice(args[1]);
    if(held.isAir()||price<=0){player.sendMessage("§cHold a sellable item and enter a valid price.");return true;}
    if(shops.setPrice(target,player,held,price))player.sendMessage("§aPrice updated to $"+EconomyManager.format(price)+" per item.");
    else player.sendMessage("§cYou do not own a player shop at that chest.");
   }
   case "remove" -> {
    if(shops.remove(target,player))player.sendMessage("§aYour player shop has been removed. Chest contents were left untouched.");
    else player.sendMessage("§cYou do not own a player shop at that chest.");
   }
   default -> help(player);
  }
  return true;
 }
 private void help(Player player){
  player.sendMessage("§6Player shops:");
  player.sendMessage("§e/pshop create <price> §7- Look at a chest while holding the item to sell.");
  player.sendMessage("§e/pshop price <price> §7- Change the held item's price at your shop.");
  player.sendMessage("§e/pshop remove §7- Remove your shop without deleting its stock.");
  player.sendMessage("§e/pshop list §7- List your shops.");
 }
 @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args){
  if(args.length==1)return List.of("create","price","remove","list").stream().filter(s->s.startsWith(args[0].toLowerCase())).toList();
  return new ArrayList<>();
 }
}
