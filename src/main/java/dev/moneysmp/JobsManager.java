package dev.moneysmp;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

public final class JobsManager {
    public enum Job { MINER, FARMER, HUNTER, FISHER }

    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final File file;
    private final YamlConfiguration data;

    public JobsManager(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.file = new File(plugin.getDataFolder(), "jobs.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public Job getJob(UUID player) {
        String value = data.getString(player.toString(), "");
        if (value.isBlank()) return null;
        try {
            return Job.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public void join(Player player, String requested) {
        Job job;
        try {
            job = Job.valueOf(requested.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            player.sendMessage("§cUnknown job. Choose Miner, Farmer, Hunter, or Fisher.");
            return;
        }
        data.set(player.getUniqueId().toString(), job.name());
        save();
        player.sendMessage("§aYou are now a §e" + title(job) + "§a. Earn money by doing that job's activities!");
    }

    public void leave(Player player) {
        if (getJob(player.getUniqueId()) == null) {
            player.sendMessage("§eYou do not currently have a job.");
            return;
        }
        data.set(player.getUniqueId().toString(), null);
        save();
        player.sendMessage("§aYou left your job.");
    }

    public void info(Player player) {
        Job job = getJob(player.getUniqueId());
        if (job == null) {
            player.sendMessage("§eYou have no job. Use §6/job join <miner|farmer|hunter|fisher>§e.");
        } else {
            player.sendMessage("§aCurrent job: §e" + title(job) + "§a. Use §e/job leave§a to leave or §e/job join <job>§a to switch.");
        }
    }

    public boolean hasJob(Player player, Job job) {
        return getJob(player.getUniqueId()) == job;
    }

    public void reward(Player player, String activity) {
        Job job = getJob(player.getUniqueId());
        if (job == null) return;
        String path = "jobs.rewards." + job.name().toLowerCase(Locale.ROOT) + "." + activity;
        String configured = plugin.getConfig().getString(path, "0.00");
        long cents = parseCents(configured);
        if (cents <= 0L) return;
        cents = economy.applyRewardMultiplier(cents);
        if (economy.deposit(player.getUniqueId(), cents)) {
            player.sendMessage("§a+" + "$" + EconomyManager.format(cents) + " §7(" + title(job) + ")");
        }
    }

    private static long parseCents(String value) {
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2).longValueExact();
        } catch (RuntimeException exception) {
            return -1L;
        }
    }

    public static boolean isOre(Material type) {
        return switch (type) {
            case COAL_ORE, DEEPSLATE_COAL_ORE, COPPER_ORE, DEEPSLATE_COPPER_ORE,
                    IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE,
                    REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE, LAPIS_ORE, DEEPSLATE_LAPIS_ORE,
                    DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, EMERALD_ORE, DEEPSLATE_EMERALD_ORE,
                    NETHER_GOLD_ORE, NETHER_QUARTZ_ORE, ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }

    private static String title(Job job) {
        String name = job.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save jobs.yml: " + exception.getMessage());
        }
    }
}
