package dev.moneysmp;
import org.bukkit.plugin.java.JavaPlugin;
public final class MoneySMP extends JavaPlugin {
 private EconomyManager economyManager;
 @Override public void onEnable(){
  saveDefaultConfig();economyManager=new EconomyManager(this);
  MoneyCommand money=new MoneyCommand(economyManager);
  if(getCommand("money")==null){getLogger().severe("Missing /money in plugin.yml; disabling.");getServer().getPluginManager().disablePlugin(this);return;}
  getCommand("money").setExecutor(money);getCommand("money").setTabCompleter(money);
  PayCommand pay=new PayCommand(economyManager);
  if(getCommand("pay")==null){getLogger().severe("Missing /pay in plugin.yml; disabling.");getServer().getPluginManager().disablePlugin(this);return;}
  getCommand("pay").setExecutor(pay);getCommand("pay").setTabCompleter(pay);
  EcoCommand eco=new EcoCommand(economyManager);
  if(getCommand("eco")==null){getLogger().severe("Missing /eco in plugin.yml; disabling.");getServer().getPluginManager().disablePlugin(this);return;}
  getCommand("eco").setExecutor(eco);getCommand("eco").setTabCompleter(eco);
  ShopManager shop=new ShopManager(this,economyManager);
  if(getCommand("shop")==null){getLogger().severe("Missing /shop in plugin.yml; disabling.");getServer().getPluginManager().disablePlugin(this);return;}
  getCommand("shop").setExecutor(new ShopCommand(shop));getServer().getPluginManager().registerEvents(new ShopListener(shop),this);
  PlayerShopManager playerShops=new PlayerShopManager(this,economyManager);PlayerShopCommand pshop=new PlayerShopCommand(playerShops);
  if(getCommand("pshop")==null){getLogger().severe("Missing /pshop in plugin.yml; disabling.");getServer().getPluginManager().disablePlugin(this);return;}
  getCommand("pshop").setExecutor(pshop);getCommand("pshop").setTabCompleter(pshop);
  getServer().getPluginManager().registerEvents(new PlayerShopListener(playerShops),this);
  AuctionManager auctions=new AuctionManager(this,economyManager);AuctionCommand ah=new AuctionCommand(auctions);
  if(getCommand("ah")==null){getLogger().severe("Missing /ah in plugin.yml; disabling.");getServer().getPluginManager().disablePlugin(this);return;}
  getCommand("ah").setExecutor(ah);getCommand("ah").setTabCompleter(ah);
  getServer().getPluginManager().registerEvents(new AuctionListener(auctions),this);
  getServer().getScheduler().runTaskTimer(this,auctions::expireListings,1200L,1200L);
  TradeManager trades=new TradeManager();TradeCommand trade=new TradeCommand(trades);
  if(getCommand("trade")==null){getLogger().severe("Missing /trade in plugin.yml; disabling.");getServer().getPluginManager().disablePlugin(this);return;}
  getCommand("trade").setExecutor(trade);getCommand("trade").setTabCompleter(trade);
  getServer().getPluginManager().registerEvents(new TradeListener(trades),this);
  getServer().getPluginManager().registerEvents(new PlayerJoinListener(economyManager),this);
  getLogger().info("MoneySMP enabled. Features 001-009: Economy, Payments, Admin Economy, Server Shop, Player Shops, Shop Management, Auction House, Player Trading, Auction Management.");
 }
 @Override public void onDisable(){if(economyManager!=null)economyManager.save();}
}
