/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptibiscuit.igates.listeners;

import com.ptibiscuit.framework.permission.PermissionHandler;
import com.ptibiscuit.igates.Plugin;
import com.ptibiscuit.igates.data.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.isCancelled()) { return; }
		Player p = e.getPlayer();
		Plugin plug = Plugin.instance;
		if (!e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation()))
		{
			Portal portal = plug.getPortalByPosition(e.getTo());
			if (portal != null)
			{
				// Il se trouve effectivement dans un portal !
				PermissionHandler perm = plug.getPermissionHandler();
				// Soit il possède la permission use, soit il possède la permissions spéciale. =)
				if (perm.has(e.getPlayer(), "portal.use", false) || perm.has(e.getPlayer(), "portal.use." + portal.getFillType().getName().toLowerCase(), false))
				{
                                    if (portal.isBungee()){
                                        System.out.println(p.getDisplayName() + "Teleporté sur un autre server.");
                                        portal.sendPlayerToServer(p);
                                    }else{
                                        System.out.println(p.getDisplayName() + "Teleporté dans ce server.");
					e.setCancelled(!portal.teleportPlayer(p));
                                    }
				} else {
					Plugin.instance.sendPreMessage(e.getPlayer(), "cant_do");
					e.setCancelled(true);
					return;
				}
				
			}
		}
	}
	
	// This is for disallowing people to use end-portal and nether-portal to go to these land.
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerUsePortal(PlayerPortalEvent e) {
		if (e.getCause() == TeleportCause.END_PORTAL || e.getCause() == TeleportCause.NETHER_PORTAL) {
			Portal p = Plugin.instance.getPortalByPosition(e.getFrom(), 0.7);
			if (p != null) {
				e.setCancelled(true);
			}
		}
	}
}
