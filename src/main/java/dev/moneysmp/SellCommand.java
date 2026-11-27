package dev.moneysmp;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class SellCommand implements CommandExecutor {
    private final MoneySMP plugin;
    private final EconomyManager economy;

    public SellCommand(MoneySMP plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can sell items.");
            return true;
        }
        if (args.length != 1 || !(args[0].equalsIgnoreCase("hand") || args[0].equalsIgnoreCase("all"))) {
            player.sendMessage("§cUsage: /sell <hand|all>");
            return true;
        }

        ConfigurationSection prices = plugin.getConfig().getConfigurationSection("sell.prices");
        if (prices == null) {
            player.sendMessage("§cSelling is not configured on this server.");
            return true;
        }

        List<Integer> slots = new ArrayList<>();
        long total = 0L;
        int itemCount = 0;
        boolean handOnly = args[0].equalsIgnoreCase("hand");
        int start = handOnly ? player.getInventory().getHeldItemSlot() : 0;
        int end = handOnly ? start + 1 : player.getInventory().getStorageContents().length;

        for (int slot = start; slot < end; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            String configured = prices.getString(item.getType().name());
            if (configured == null) continue;
            try {
                long unitPrice = new BigDecimal(configured).setScale(2, RoundingMode.UNNECESSARY)
                        .movePointRight(2).longValueExact();
                if (unitPrice <= 0) continue;
                long stackValue = Math.multiplyExact(unitPrice, item.getAmount());
                total = Math.addExact(total, stackValue);
                itemCount += item.getAmount();
                slots.add(slot);
            } catch (ArithmeticException | NumberFormatException ignored) {
                plugin.getLogger().warning("Invalid sell price for " + item.getType() + " in config.yml");
            }
        }

        if (slots.isEmpty() || total <= 0) {
            player.sendMessage("§cYou don't have any items in that selection that can be sold.");
            return true;
        }
        if (!economy.deposit(player.getUniqueId(), total)) {
            player.sendMessage("§cThe sale could not be completed. Keep your items and try again.");
            return true;
        }
        for (int slot : slots) player.getInventory().setItem(slot, null);
        player.sendMessage("§aSold " + itemCount + " item(s) for $" + EconomyManager.format(total) + ".");
        return true;
    }
}
