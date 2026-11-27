package dev.moneysmp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public final class ShopCommand implements CommandExecutor {
 private final ShopManager shop;
 public ShopCommand(ShopManager shop){this.shop=shop;}
 @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
  if(!(sender instanceof Player player)){sender.sendMessage("Only players can open the shop.");return true;}
  shop.open(player); return true;
 }
}
