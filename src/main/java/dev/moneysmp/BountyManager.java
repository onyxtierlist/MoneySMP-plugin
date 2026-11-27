package dev.moneysmp;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BountyManager {
    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final File file;
    private final YamlConfiguration data;

    public BountyManager(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.file = new File(plugin.getDataFolder(), "bounties.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void place(Player owner, Player target, long amountCents) {
        if (owner.getUniqueId().equals(target.getUniqueId())) {
            owner.sendMessage("§cYou cannot place a bounty on yourself.");
            return;
        }
        if (amountCents <= 0L || amountCents < plugin.getConfig().getLong("bounty.minimum-cents", 100L)) {
            owner.sendMessage("§cThe bounty is below the configured minimum.");
            return;
        }
        if (!economy.withdraw(owner.getUniqueId(), amountCents)) {
            owner.sendMessage("§cYou do not have enough money for that bounty.");
            return;
        }
        int id = data.getInt("next-id", 1);
        data.set("next-id", id + 1);
        String path = "bounties." + id;
        data.set(path + ".owner", owner.getUniqueId().toString());
        data.set(path + ".owner-name", owner.getName());
        data.set(path + ".target", target.getUniqueId().toString());
        data.set(path + ".target-name", target.getName());
        data.set(path + ".amount", amountCents);
        data.set(path + ".created", System.currentTimeMillis());
        save();
        owner.sendMessage("§aPlaced bounty #" + id + " of §6$" + EconomyManager.format(amountCents)
                + " §aon " + target.getName() + ".");
        target.sendMessage("§eA bounty has been placed on you!");
    }

    public boolean cancel(Player owner, int id) {
        String path = "bounties." + id;
        if (!data.isConfigurationSection(path)
                || !owner.getUniqueId().toString().equals(data.getString(path + ".owner"))) return false;
        long amount = data.getLong(path + ".amount", -1L);
        if (amount <= 0L || !economy.deposit(owner.getUniqueId(), amount)) return false;
        data.set(path, null);
        save();
        owner.sendMessage("§aBounty #" + id + " cancelled. $"
                + EconomyManager.format(amount) + " was refunded.");
        return true;
    }

    public List<String> list() {
        List<String> result = new ArrayList<>();
        ConfigurationSection section = data.getConfigurationSection("bounties");
        if (section == null) return result;
        for (String id : section.getKeys(false)) {
            String path = "bounties." + id;
            result.add("§e#" + id + " §f" + data.getString(path + ".target-name", "Unknown")
                    + " §7— §6$" + EconomyManager.format(data.getLong(path + ".amount", 0L))
                    + " §7(placed by " + data.getString(path + ".owner-name", "Unknown") + ")");
        }
        return result;
    }

    public void claim(Player killer, Player target) {
        ConfigurationSection section = data.getConfigurationSection("bounties");
        if (section == null) return;
        List<String> ids = new ArrayList<>(section.getKeys(false));
        long total = 0L;
        int count = 0;
        for (String id : ids) {
            String path = "bounties." + id;
            if (!target.getUniqueId().toString().equals(data.getString(path + ".target"))) continue;
            if (killer.getUniqueId().toString().equals(data.getString(path + ".owner"))) continue;
            long amount = data.getLong(path + ".amount", -1L);
            if (amount <= 0L || !economy.deposit(killer.getUniqueId(), amount)) continue;
            total = total > Long.MAX_VALUE - amount ? Long.MAX_VALUE : total + amount;
            count++;
            data.set(path, null);
        }
        if (count > 0) {
            save();
            killer.sendMessage("§aYou claimed " + count + " bounty/bounties for §6$"
                    + EconomyManager.format(total) + "§a!");
        }
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save bounties.yml: " + exception.getMessage());
        }
    }
}
