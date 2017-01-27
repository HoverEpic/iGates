package com.ptibiscuit.igates.listeners;

import com.ptibiscuit.igates.IGates;
import com.ptibiscuit.igates.data.Volume;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class VolumeSelectionManager implements Listener
{

    private Material selectionTools = Material.WOOD_AXE;
    private HashMap<String, Volume> selections = new HashMap<String, Volume>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.getItem() != null && e.getItem().getType() == selectionTools && e.getClickedBlock() != null
                && e.getPlayer().hasPermission("make_selection")) {
            Volume v = this.selections.get(e.getPlayer().getName());
            if (v == null) {
                v = new Volume(null, null);
                this.selections.put(e.getPlayer().getName(), v);
            }
            Location l = e.getClickedBlock().getLocation();
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                v.setEnd(l);
                if (IGates.getPlugin().getConfig().getBoolean("config.display_message_selection", true)) {
                    IGates.getPlugin().sendTranslation(e.getPlayer(), "second_point_set");
                }
            }
            else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                v.setFirst(l);
                if (IGates.getPlugin().getConfig().getBoolean("config.display_message_selection", true)) {
                    IGates.getPlugin().sendTranslation(e.getPlayer(), "first_point_set");
                }
            }
        }
    }

    public Volume getSelection(String player)
    {
        Volume v = this.selections.get(player);
        if (v != null && v.getFirst() != null && v.getEnd() != null) {
            return v;
        }
        return null;
    }

    public void removeSelection(String p)
    {
        this.selections.remove(p);
    }
}
