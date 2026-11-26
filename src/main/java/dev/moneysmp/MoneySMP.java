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

        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(economyManager), this);

        getLogger().info("MoneySMP enabled. Feature 001: Basic Economy.");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.save();
        }
    }
}
