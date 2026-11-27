package dev.moneysmp;

import org.bukkit.plugin.java.JavaPlugin;

public final class MoneySMP extends JavaPlugin {
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        economyManager = new EconomyManager(this);

        MoneyCommand money = new MoneyCommand(economyManager);
        if (getCommand("money") == null) { disableForMissingCommand("money"); return; }
        getCommand("money").setExecutor(money);
        getCommand("money").setTabCompleter(money);

        PayCommand pay = new PayCommand(economyManager);
        if (getCommand("pay") == null) { disableForMissingCommand("pay"); return; }
        getCommand("pay").setExecutor(pay);
        getCommand("pay").setTabCompleter(pay);

        EcoCommand eco = new EcoCommand(economyManager);
        if (getCommand("eco") == null) { disableForMissingCommand("eco"); return; }
        getCommand("eco").setExecutor(eco);
        getCommand("eco").setTabCompleter(eco);

        ShopManager shop = new ShopManager(this, economyManager);
        if (getCommand("shop") == null) { disableForMissingCommand("shop"); return; }
        getCommand("shop").setExecutor(new ShopCommand(shop));
        getServer().getPluginManager().registerEvents(new ShopListener(shop), this);

        PlayerShopManager playerShops = new PlayerShopManager(this, economyManager);
        PlayerShopCommand pshop = new PlayerShopCommand(playerShops);
        if (getCommand("pshop") == null) { disableForMissingCommand("pshop"); return; }
        getCommand("pshop").setExecutor(pshop);
        getCommand("pshop").setTabCompleter(pshop);
        getServer().getPluginManager().registerEvents(new PlayerShopListener(playerShops), this);

        AuctionManager auctions = new AuctionManager(this, economyManager);
        AuctionCommand ah = new AuctionCommand(auctions);
        if (getCommand("ah") == null) { disableForMissingCommand("ah"); return; }
        getCommand("ah").setExecutor(ah);
        getCommand("ah").setTabCompleter(ah);
        getServer().getPluginManager().registerEvents(new AuctionListener(auctions), this);
        getServer().getScheduler().runTaskTimer(this, auctions::expireListings, 1200L, 1200L);

        TradeManager trades = new TradeManager();
        TradeCommand trade = new TradeCommand(trades);
        if (getCommand("trade") == null) { disableForMissingCommand("trade"); return; }
        getCommand("trade").setExecutor(trade);
        getCommand("trade").setTabCompleter(trade);
        getServer().getPluginManager().registerEvents(new TradeListener(trades), this);

        DailyRewardManager dailyRewards = new DailyRewardManager(this, economyManager);
        if (getCommand("daily") == null) { disableForMissingCommand("daily"); return; }
        getCommand("daily").setExecutor(new DailyCommand(dailyRewards));

        JobsManager jobs = new JobsManager(this, economyManager);
        JobCommand job = new JobCommand(jobs);
        if (getCommand("job") == null) { disableForMissingCommand("job"); return; }
        getCommand("job").setExecutor(job);
        getCommand("job").setTabCompleter(job);
        getServer().getPluginManager().registerEvents(new JobListener(jobs), this);

        BountyManager bounties = new BountyManager(this, economyManager);
        BountyCommand bounty = new BountyCommand(bounties);
        if (getCommand("bounty") == null) { disableForMissingCommand("bounty"); return; }
        getCommand("bounty").setExecutor(bounty);
        getCommand("bounty").setTabCompleter(bounty);
        getServer().getPluginManager().registerEvents(new BountyListener(bounties), this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(economyManager), this);
        getLogger().info("MoneySMP enabled. Features 001-012 loaded.");
    }

    private void disableForMissingCommand(String command) {
        getLogger().severe("Missing /" + command + " in plugin.yml; disabling.");
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        if (economyManager != null) economyManager.save();
    }
}
