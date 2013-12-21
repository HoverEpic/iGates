package com.ptibiscuit.igates;

import com.ptibiscuit.framework.JavaPluginEnhancer;
import com.ptibiscuit.igates.data.FillType;
import com.ptibiscuit.igates.data.Portal;
import com.ptibiscuit.igates.data.Volume;
import com.ptibiscuit.igates.data.models.IData;
import com.ptibiscuit.igates.data.models.YamlData;
import com.ptibiscuit.igates.listeners.PlayerListener;
import com.ptibiscuit.igates.listeners.SpreadBlockListener;
import com.ptibiscuit.igates.listeners.VolumeSelectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Plugin extends JavaPluginEnhancer implements Listener {
	public static Plugin instance;
	
	private IData data;
	private VolumeSelectionManager vsm = new VolumeSelectionManager();
	private SpreadBlockListener sbl = new SpreadBlockListener();
	private PlayerListener pm = new PlayerListener();
	private Economy economy;
	
	
	@Override
	public void onConfigurationDefault(FileConfiguration c) {
		c.set("portals", new HashMap<String, Object>());
		c.set("config.retain_liquid", false);
                c.set("config.InventorySQL_support", false);
		c.set("config.display_message_selection", true);
	}

	@Override
	public void onEnable() {
		this.setup(ChatColor.AQUA + "[iGates]", "igates", true);
		this.instance = this;
		this.myLog.startFrame();
		this.myLog.addInFrame("iGates by Ptibiscuit");
                this.myLog.addInFrame("Modified for Hovercarft");
		this.myLog.addCompleteLineInFrame();
		
		this.data = new YamlData();
		// On fait attention à Multiverse, au cas où.
		data.loadPortals();
		this.myLog.addInFrame(data.getPortals().size() + " portals loaded !");
		// Enable Economic support
		if (this.setupEconomy())
			this.myLog.addInFrame("Economy enabled !", true);
		else
			this.myLog.addInFrame("Can't enable Economy. It wasn't necessary, but it's sad ... :'(", false);
		PluginManager pgm = this.getServer().getPluginManager();
		pgm.registerEvents(vsm, this);
		pgm.registerEvents(pm, this);
		pgm.registerEvents(sbl, this);
		pgm.registerEvents(this, this);
		
		this.myLog.displayFrame(false);
                //register BungeeCord chanel to send server command
                getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	}
	
	@Override
	public void onLangDefault(Properties p) {
		p.setProperty("cant_do", "You're not able to do that.");
		p.setProperty("need_be_player", "You need to be a player to do that.");
		p.setProperty("more_args", "Bad using of the command (Need argument ?).");
		p.setProperty("tag_taken", "This portal's tag ios already taken.");
		p.setProperty("ft_dont_exist", "This FillType doesn't exist (water, portal, end_portal, lava, web)");
		p.setProperty("tag_dont_exist", "This portal's tag doesn't exist.");
		p.setProperty("set_active", "This portal has been turned {ACTIVE}.");
		p.setProperty("set_bungee", "This portal is now bungee ({SWITCH}).");
		p.setProperty("set_filltype", "This portal's filltype is now {FILLTYPE}.");
		p.setProperty("set_to", "The new \"to\" point of the portal is set.");
                p.setProperty("set_server", "The new \"to\" server of the portal is set.");
		p.setProperty("portal_deleted", "Portal deleted. :'(");
		p.setProperty("first_point_set", "The first point of your selection is set !");
		p.setProperty("second_point_set", "The second point of your selection is set !");
		p.setProperty("froms_added", "\"From area\" added to the portal !");
		p.setProperty("need_volume", "Before do that, you need to select an area with the woodaxe, like with WorldEdit. =)");
		p.setProperty("top_list", "List of all portals:");
		p.setProperty("elem_list", "{ACTIVE} " + ChatColor.GOLD + "{TAG}" + ChatColor.WHITE + ": {CNT_FROMS} \"Froms\" areas.");
		p.setProperty("portail_created", "Portal \"{TAG}\" created !");
		p.setProperty("weird_arg", "You used weird arg, only on, off, to and filltype are available.");
		p.setProperty("cant_afford", "You don't have enough money to take this portal. It costs {PRICE}.");
		p.setProperty("pay_the_price", "You have paid {PRICE} to take this portal.");
		p.setProperty("set_price", "The new price of the portal is set !");
	}

	@Override
	public void onDisable() {
		
	}

	public boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		this.economy = rsp.getProvider();
		return economy != null;
	}
	
	public Economy getEconomy()
	{
		return this.economy;
	}
	
	public boolean isEconomyEnabled()
	{
		return this.economy != null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try
		{
			if (!(sender instanceof Player))
			{
				this.sendPreMessage(sender, "need_be_player");
				return true;
			}
			Player p = (Player) sender;
			
			if (label.equalsIgnoreCase("igcreate"))
			{
				if (!this.permissionHandler.has(sender, "portal.edit.create", true))
				{
					this.sendPreMessage(sender, "cant_do");
					return true;
				}
				
				if (this.getPortal(args[0]) == null)
				{
					FillType ft = FillType.getFillType(args[1]);
					if (ft != null)
					{
						data.createPortal(args[0], p.getLocation(), new ArrayList<Volume>(), ft);
						this.sendMessage(sender, getSentence("portail_created").replace("{TAG}", args[0]));
					}
					else
					{
						this.sendPreMessage(sender, "ft_dont_exist");
					}
				}
				else
				{
					this.sendPreMessage(sender, "tag_taken");
				}
			}
			else if (label.equalsIgnoreCase("igset"))
			{
				if (!this.permissionHandler.has(sender, "portal.edit.set." + args[0], true))
				{
					this.sendPreMessage(sender, "cant_do");
					return true;
				}
				
				Portal portal = this.getPortal(args[1]);
				if (portal == null)
				{
					this.sendPreMessage(sender, "tag_dont_exist");
					return true;
				}
				
				if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))
				{
					boolean active = (args[0].equalsIgnoreCase("on")) ? true : false; 
					this.data.setActive(portal, active);
					this.sendMessage(sender, this.getSentence("set_active").replace("{ACTIVE}", args[0]));
				}
                                else if (args[0].equalsIgnoreCase("bungee"))
				{
					boolean bungee = (args[2].equalsIgnoreCase("on")) ? true : false; 
					this.data.setBungee(portal, bungee);
					this.sendMessage(sender, this.getSentence("set_bungee").replace("{SWITCH}", args[2]));
				}
				else if (args[0].equalsIgnoreCase("price"))
				{
					int price = new Integer(args[2]);
					this.data.setPrice(portal, price);
					this.sendPreMessage(sender, "set_price");
				}
				else if (args[0].equalsIgnoreCase("to"))
				{
					this.data.setSpawn(portal, p.getLocation());
					this.sendMessage(sender, this.getSentence("set_to"));
				}
                                else if (args[0].equalsIgnoreCase("server"))
				{
					this.data.setServer(portal, args[2]);
					this.sendMessage(sender, this.getSentence("set_server"));
				}
				else if (args[0].equalsIgnoreCase("filltype"))
				{
					FillType ft = FillType.getFillType(args[2]);
					if (ft != null)
					{
						this.data.setFillType(portal, ft);
						this.sendMessage(sender, this.getSentence("set_filltype").replace("{FILLTYPE}", args[2]));
					}
					else
					{
						this.sendPreMessage(sender, "ft_dont_exist");
					}
				}
				else
				{
					this.sendPreMessage(sender, "weird_arg");
				}
			}
			else if (label.equalsIgnoreCase("igdelete"))
			{
				if (!this.permissionHandler.has(sender, "portal.edit.delete", true))
				{
					this.sendPreMessage(sender, "cant_do");
					return true;
				}
				
				Portal portal = this.getPortal(args[0]);
				if (portal == null)
				{
					this.sendPreMessage(sender, "tag_dont_exist");
					return true;
				}
				
				portal.beforeDelete();
				this.data.deletePortal(portal);
				this.sendPreMessage(sender, "portal_deleted");
			}
			else if (label.equalsIgnoreCase("iglist"))
			{
				if (!this.permissionHandler.has(sender, "portal.list", false))
				{
					this.sendPreMessage(sender, "cant_do");
					return true;
				}
				
				this.sendPreMessage(sender, "top_list");
				for (Portal portal : this.data.getPortals())
				{
					String enable = (portal.isActive()) ? ChatColor.GREEN + "[V]" : ChatColor.RED + "[X]" ;
					enable += ChatColor.WHITE;
					int cntFroms = portal.getFromPoints().size();
					this.sendMessage(sender, getSentence("elem_list").replace("{TAG}", portal.getTag()).replace("{ACTIVE}", enable).replace("{CNT_FROMS}", String.valueOf(cntFroms)));
				}
			}
			else if (label.equalsIgnoreCase("igaddfrom"))
			{
				if (!this.permissionHandler.has(sender, "portal.edit.addfrom", true))
				{
					this.sendPreMessage(sender, "cant_do");
					return true;
				}
				
				Portal portal = this.getPortal(args[0]);
				if (portal == null)
				{
					this.sendPreMessage(sender, "tag_dont_exist");
					return true;
				}
				Volume v = this.vsm.getSelection(p.getName());
				if (v != null)
				{
					portal.getFromPoints().add(v);
					this.data.setFromsAreas(portal, portal.getFromPoints());
					this.vsm.removeSelection(p.getName());
					this.sendPreMessage(sender, "froms_added");
				}
				else
				{
					this.sendPreMessage(sender, "need_volume");
				}
			}
			else if (label.equalsIgnoreCase("igclearfroms"))
			{
				if (!this.permissionHandler.has(sender, "portal.edit.clearfroms", true))
				{
					this.sendPreMessage(sender, "cant_do");
					return true;
				}
				
				Portal portal = this.getPortal(args[0]);
				if (portal == null)
				{
					this.sendPreMessage(sender, "tag_dont_exist");
					return true;
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			this.sendPreMessage(sender, "more_args");
		}
		return true;
	}
	
	public Portal getPortalByPosition(Location l, double offset)
	{
		for (Portal p : this.data.getPortals())
		{
			if (p.isIn(l, offset) && p.isActive())
				return p;
		}
		return null;
	}
	
	public Portal getPortalByPosition(Location l) {
		return this.getPortalByPosition(l, 0);
	}
	
	public Portal getPortal(String tag)
	{
		for (Portal p : this.data.getPortals())
		{
			if (p.getTag().equalsIgnoreCase(tag))
				return p;
		}
		return null;
	}

	public IData getData() {
		return data;
	}
}