package dev.moneysmp;
import org.bukkit.command.Command;import org.bukkit.command.CommandExecutor;import org.bukkit.command.CommandSender;import org.bukkit.command.TabCompleter;import org.bukkit.entity.Player;
import java.util.ArrayList;import java.util.List;
public final class AuctionCommand implements CommandExecutor,TabCompleter{
 private final AuctionManager auctions;public AuctionCommand(AuctionManager auctions){this.auctions=auctions;}
 @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
  if(!(sender instanceof Player p)){sender.sendMessage("Only players can use the auction house.");return true;}
  if(args.length==0){auctions.open(p);return true;}
  switch(args[0].toLowerCase()){
   case "sell"->{if(args.length<2||args.length>3){p.sendMessage("§eUsage: /ah sell <price> [amount]");return true;}
    long price=AuctionManager.parsePrice(args[1]);int amount=1;
    if(args.length==3)try{amount=Integer.parseInt(args[2]);}catch(NumberFormatException ex){amount=-1;}
    if(price<=0||amount<=0){p.sendMessage("§cEnter a valid price and amount.");return true;}auctions.sell(p,price,amount);}
   case "cancel"->{if(args.length!=2){p.sendMessage("§eUsage: /ah cancel <id>");return true;}try{if(!auctions.cancel(p,Integer.parseInt(args[1])))p.sendMessage("§cYou do not own that active listing.");}catch(NumberFormatException ex){p.sendMessage("§cListing ID must be a number.");}}
   case "mine"->{List<String> mine=auctions.ownedListings(p.getUniqueId());p.sendMessage("§6Your active auctions ("+mine.size()+"):");if(mine.isEmpty())p.sendMessage("§7None.");else mine.forEach(p::sendMessage);}
   default->p.sendMessage("§e/ah §7- Open auction house | §e/ah sell <price> [amount] §7- List held item | §e/ah mine §7- Your listings | §e/ah cancel <id> §7- Cancel a listing");
  }return true;
 }
 @Override public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){if(args.length==1)return List.of("sell","mine","cancel").stream().filter(x->x.startsWith(args[0].toLowerCase())).toList();return new ArrayList<>();}
}
