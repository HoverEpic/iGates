package com.ptibiscuit.igates.listeners;

import com.ptibiscuit.igates.IGates;
import com.ptibiscuit.igates.data.Portal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

public class SpreadBlockListener implements Listener
{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent e)
    {
        if (IGates.getPlugin().getConfig().getBoolean("config.retain_liquid")) {
            for (Portal p : IGates.getPlugin().getData().getPortals()) {
                if (p.isIn(e.getBlock().getLocation()) && !p.isIn(e.getToBlock().getLocation())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPhysics(BlockPhysicsEvent e)
    {
        for (Portal p : IGates.getPlugin().getData().getPortals()) {
            if (p.isIn(e.getBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
