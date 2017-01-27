/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptibiscuit.igates.listeners;

import com.ptibiscuit.igates.IGates;
import com.ptibiscuit.igates.data.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerListener implements Listener
{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e)
    {
        if (e.isCancelled()) {
            return;
        }
        Player p = e.getPlayer();
        if (!e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation())) {
            Portal portal = IGates.getPlugin().getPortalByPosition(e.getTo());
            if (portal != null) {
                // Il se trouve effectivement dans un portal !
                // Soit il possède la permission use, soit il possède la permissions spéciale. =)
                if (e.getPlayer().hasPermission("portal.use") || e.getPlayer().hasPermission("portal.use." + portal.getFillType().getName().toLowerCase())) {
                    if (portal.isBungee()) {
                        portal.sendPlayerToServer(p);
                    }
                    else {
                        e.setCancelled(!portal.teleportPlayer(p));
                    }
                }
                else {
                    IGates.getPlugin().sendTranslation(e.getPlayer(), "cant_do");
                    e.setCancelled(true);
                    return;
                }

            }
        }
    }

    // This is for disallowing people to use end-portal and nether-portal to go to these land.
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUsePortal(PlayerPortalEvent e)
    {
        if (e.getCause() == TeleportCause.END_PORTAL || e.getCause() == TeleportCause.NETHER_PORTAL) {
            Portal p = IGates.getPlugin().getPortalByPosition(e.getFrom(), 0.7);
            if (p != null) {
                e.setCancelled(true);
            }
        }
    }

    // Pour téléporter au spawn un joueur qui arrive dans un portail.
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player playerJoined = e.getPlayer();
        Portal p = IGates.getPlugin().getPortalByPosition(playerJoined.getLocation(), 0.7);
        Location s = playerJoined.getWorld().getSpawnLocation();
        if (p != null) {
            playerJoined.teleport(s);
        }
    }
}
