package dev.moneysmp;

import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public final class JobListener implements Listener {
    private final JobsManager jobs;

    public JobListener(JobsManager jobs) {
        this.jobs = jobs;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();
        if (jobs.hasJob(player, JobsManager.Job.MINER) && JobsManager.isOre(type)) {
            jobs.reward(player, "ore");
            return;
        }
        if (!jobs.hasJob(player, JobsManager.Job.FARMER)) return;
        if (!(event.getBlock().getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < ageable.getMaximumAge()) return;
        if (type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES
                || type == Material.BEETROOTS || type == Material.NETHER_WART
                || type == Material.COCOA || type == Material.SWEET_BERRY_BUSH) {
            jobs.reward(player, "crop");
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        Entity entity = event.getEntity();
        if (killer != null && entity instanceof Monster && jobs.hasJob(killer, JobsManager.Job.HUNTER)) {
            jobs.reward(killer, "mob");
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == State.CAUGHT_FISH && event.getCaught() != null
                && event.getCaught() instanceof org.bukkit.entity.Item
                && jobs.hasJob(event.getPlayer(), JobsManager.Job.FISHER)) {
            jobs.reward(event.getPlayer(), "fish");
        }
    }
}
