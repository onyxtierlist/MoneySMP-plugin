package dev.moneysmp;
import org.bukkit.Bukkit;import org.bukkit.entity.Player;import org.bukkit.inventory.ItemStack;
import java.util.HashMap;import java.util.Map;import java.util.UUID;
public final class TradeManager{
 private record Request(UUID sender,long expires){}
 private static final class Session{final UUID a,b;ItemStack offerA,offerB;boolean confirmA,confirmB;Session(UUID a,UUID b){this.a=a;this.b=b;}}
 private final Map<UUID,Request> requests=new HashMap<>();private final Map<UUID,Session> sessions=new HashMap<>();
 public void request(Player from,Player to){
  if(from.equals(to)){from.sendMessage("§cYou cannot trade with yourself.");return;}
  if(sessions.containsKey(from.getUniqueId())||sessions.containsKey(to.getUniqueId())){from.sendMessage("§cOne of you is already trading.");return;}
  requests.put(to.getUniqueId(),new Request(from.getUniqueId(),System.currentTimeMillis()+60_000));
  from.sendMessage("§aTrade request sent to "+to.getName()+".");to.sendMessage("§e"+from.getName()+" wants to trade. Use /trade accept within 60 seconds.");
 }
 public void accept(Player target){
  Request req=requests.remove(target.getUniqueId());if(req==null||req.expires<System.currentTimeMillis()){target.sendMessage("§cYou have no active trade request.");return;}
  Player sender=Bukkit.getPlayer(req.sender);if(sender==null){target.sendMessage("§cThat player is no longer online.");return;}
  if(sessions.containsKey(sender.getUniqueId())||sessions.containsKey(target.getUniqueId())){target.sendMessage("§cOne of you is already trading.");return;}
  Session s=new Session(sender.getUniqueId(),target.getUniqueId());sessions.put(s.a,s);sessions.put(s.b,s);
  sender.sendMessage("§aTrade started with "+target.getName()+". Hold the item you want to offer and use /trade offer.");
  target.sendMessage("§aTrade started with "+sender.getName()+". Hold the item you want to offer and use /trade offer.");
 }
 public void offer(Player p){
  Session s=sessions.get(p.getUniqueId());if(s==null){p.sendMessage("§cYou are not in a trade.");return;}
  boolean a=p.getUniqueId().equals(s.a);if((a?s.offerA:s.offerB)!=null){p.sendMessage("§cYou already made an offer. Confirm or cancel this trade.");return;}
  ItemStack hand=p.getInventory().getItemInMainHand();if(hand.getType().isAir()){p.sendMessage("§cHold the item stack you want to offer.");return;}
  ItemStack copy=hand.clone();p.getInventory().setItemInMainHand(null);if(a)s.offerA=copy;else s.offerB=copy;s.confirmA=false;s.confirmB=false;
  notify(s,"§e"+p.getName()+" added an item offer. Both players must use /trade confirm.");
 }
 public void confirm(Player p){
  Session s=sessions.get(p.getUniqueId());if(s==null){p.sendMessage("§cYou are not in a trade.");return;}
  if(s.offerA==null||s.offerB==null){p.sendMessage("§cBoth players must offer an item first.");return;}
  if(p.getUniqueId().equals(s.a))s.confirmA=true;else s.confirmB=true;
  notify(s,"§e"+p.getName()+" confirmed ("+(s.confirmA?1:0)+"/1, "+(s.confirmB?1:0)+"/1).");
  if(s.confirmA&&s.confirmB){
   Player a=Bukkit.getPlayer(s.a),b=Bukkit.getPlayer(s.b);
   if(a==null||b==null){cancelSession(s,"§cTrade cancelled because a player disconnected.");return;}
   ItemStack toA=s.offerB.clone(),toB=s.offerA.clone();remove(s);
   giveOrDrop(a,toA);giveOrDrop(b,toB);a.sendMessage("§aTrade completed!");b.sendMessage("§aTrade completed!");
  }
 }
 public void cancel(Player p){
  Session s=sessions.get(p.getUniqueId());if(s!=null){cancelSession(s,"§cTrade cancelled.");return;}
  requests.remove(p.getUniqueId());requests.values().removeIf(req->req.sender.equals(p.getUniqueId()));p.sendMessage("§eTrade request cancelled.");
 }
 public void quit(Player p){
  requests.remove(p.getUniqueId());requests.values().removeIf(req->req.sender.equals(p.getUniqueId()));
  Session s=sessions.get(p.getUniqueId());if(s==null)return;
  boolean a=p.getUniqueId().equals(s.a);ItemStack own=a?s.offerA:s.offerB;ItemStack partner=a?s.offerB:s.offerA;
  UUID partnerId=a?s.b:s.a;Player other=Bukkit.getPlayer(partnerId);remove(s);
  giveOrDrop(p,own);if(other!=null){giveOrDrop(other,partner);other.sendMessage("§cTrade cancelled because a player disconnected.");}
 }
 private void cancelSession(Session s,String message){
  Player a=Bukkit.getPlayer(s.a),b=Bukkit.getPlayer(s.b);remove(s);
  if(a!=null){giveOrDrop(a,s.offerA);a.sendMessage(message);}
  if(b!=null){giveOrDrop(b,s.offerB);b.sendMessage(message);}
 }
 private void giveOrDrop(Player p,ItemStack item){if(item==null)return;for(ItemStack leftover:p.getInventory().addItem(item).values())p.getWorld().dropItemNaturally(p.getLocation(),leftover);}
 private void notify(Session s,String msg){Player a=Bukkit.getPlayer(s.a),b=Bukkit.getPlayer(s.b);if(a!=null)a.sendMessage(msg);if(b!=null)b.sendMessage(msg);}
 private void remove(Session s){sessions.remove(s.a);sessions.remove(s.b);}
}
