package dev.moneysmp;
import org.bukkit.Bukkit;import org.bukkit.command.Command;import org.bukkit.command.CommandExecutor;import org.bukkit.command.CommandSender;import org.bukkit.command.TabCompleter;import org.bukkit.entity.Player;
import java.util.ArrayList;import java.util.List;
public final class TradeCommand implements CommandExecutor,TabCompleter{
 private final TradeManager trades;public TradeCommand(TradeManager trades){this.trades=trades;}
 @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
  if(!(sender instanceof Player p)){sender.sendMessage("Only players can trade.");return true;}
  if(args.length==0){p.sendMessage("§e/trade <player> | /trade accept | /trade offer | /trade confirm | /trade cancel");return true;}
  switch(args[0].toLowerCase()){
   case "accept"->{trades.accept(p);}
   case "offer"->{trades.offer(p);}
   case "confirm"->{trades.confirm(p);}
   case "cancel"->{trades.cancel(p);}
   default->{Player target=Bukkit.getPlayerExact(args[0]);if(target==null){p.sendMessage("§cThat player must be online.");return true;}trades.request(p,target);}
  }return true;
 }
 @Override public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){
  if(args.length==1){List<String> out=new ArrayList<>(List.of("accept","offer","confirm","cancel"));for(Player p:Bukkit.getOnlinePlayers())out.add(p.getName());return out.stream().filter(x->x.toLowerCase().startsWith(args[0].toLowerCase())).toList();}return new ArrayList<>();
 }
}
