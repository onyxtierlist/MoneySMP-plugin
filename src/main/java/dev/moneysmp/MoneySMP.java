package dev.moneysmp;

import org.bukkit.plugin.java.JavaPlugin;

public final class MoneySMP extends JavaPlugin {
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        economyManager = new EconomyManager(this);

        MoneyCommand moneyCommand = new MoneyCommand(economyManager);
        if (getCommand("money") == null) {
            getLogger().severe("The /money command is missing from plugin.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("money").setExecutor(moneyCommand);
        getCommand("money").setTabCompleter(moneyCommand);

        PayCommand payCommand = new PayCommand(economyManager);
        if (getCommand("pay") == null) {
            getLogger().severe("The /pay command is missing from plugin.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("pay").setExecutor(payCommand);
        getCommand("pay").setTabCompleter(payCommand);

        EcoCommand ecoCommand = new EcoCommand(economyManager);
        if (getCommand("eco") == null) {
            getLogger().severe("The /eco command is missing from plugin.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("eco").setExecutor(ecoCommand);
        getCommand("eco").setTabCompleter(ecoCommand);

        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(economyManager), this);

        getLogger().info("MoneySMP enabled. Features 001-003: Economy, Payments, and Admin Economy.");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.save();
        }
    }
}
