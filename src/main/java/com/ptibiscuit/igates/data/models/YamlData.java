/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptibiscuit.igates.data.models;

import com.ptibiscuit.igates.IGates;
import com.ptibiscuit.igates.data.FillType;
import com.ptibiscuit.igates.data.Portal;
import com.ptibiscuit.igates.data.Volume;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class YamlData implements IData {

	private ArrayList<Portal> portals = new ArrayList<Portal>();
	private FileConfiguration config;
	
	@Override
	public Portal createPortal(String tag, Location to, ArrayList<Volume> froms, FillType fillType) {
		Portal portal = new Portal(tag, to, "", froms, 0, fillType, false, false);
		config.set("portals." + portal.getTag() + ".enable", false);
                config.set("portals." + portal.getTag() + ".bungee", false);
		config.set("portals." + portal.getTag() + ".to", this.convertLocationToString(to));
                config.set("portals." + portal.getTag() + ".server", "");
		config.set("portals." + portal.getTag() + ".yaw", to.getYaw());
		config.set("portals." + portal.getTag() + ".pitch", to.getPitch());
		config.set("portals." + portal.getTag() + ".filltype", fillType.getName());
		IGates.getPlugin().saveConfig();
		
		portals.add(portal);
		return portal;
	}

	@Override
	public void loadPortals() {
		config = IGates.getPlugin().getConfig();
		if (config.getConfigurationSection("portals") != null)
		{
			for (Entry<String, Object> entry : config.getConfigurationSection("portals").getValues(false).entrySet())
			{
				String tag = entry.getKey();
				ConfigurationSection values = (ConfigurationSection) entry.getValue();
				// Loader le point To
				Location to = null;
                                String toServer = "";
				if (values.get("to") != null)
				{
					to = this.convertStringToLocation(values.get("to").toString());
					if (values.getString("pitch") != null)
						to.setPitch(Float.parseFloat(values.getString("pitch")));
					if (values.getString("yaw") != null)
						to.setYaw(Float.parseFloat(values.getString("yaw")));
				}
                                if (values.get("server") != null)
				{
                                    toServer = values.get("server").toString();
				}
				// Getting the price
				int price = 0;
				if (values.get("price") != null)
					price = values.getInt("price");
				// Loader le FillType
				FillType filltype = FillType.getFillType(values.getString("filltype"));
				if (filltype == null)
				{
					IGates.getPlugin().getLogger().severe(tag + " is in an old portal's configuration, modify it !");
				}
				// Loader les froms
				ArrayList<Volume> froms = new ArrayList<Volume>();
				if (values.get("froms") != null)
				{
					for (HashMap<String, Object> from : (ArrayList<HashMap<String, Object>>) values.get("froms"))
					{
						froms.add(new Volume(convertStringToLocation(from.get("begin").toString()), convertStringToLocation(from.get("end").toString())));
					}
				}
				// Loader l'active
				boolean active = values.getBoolean("enable");
                                boolean switchServer = values.getBoolean("bungee");
				Portal p = new Portal(tag, to,toServer, froms, price, filltype, active, switchServer);
				portals.add(p);
			}
		}
	}

	@Override
	public ArrayList<Portal> getPortals() {
		return portals;
	}

	@Override
	public void deletePortal(Portal portal) {
		config.set("portals." + portal.getTag(), null);
		IGates.getPlugin().saveConfig();
		portals.remove(portal);
	}
	
	public Location convertStringToLocation(String info)
	{
		String[] has = info.split("/");
		Location loc = new Location(Bukkit.getWorld(has[0]), Double.parseDouble(has[1]), Double.parseDouble(has[2]), Double.parseDouble(has[3]));
		return loc;
	}
	
	public String convertLocationToString(Location info)
	{
		return info.getWorld().getName() + "/" + info.getX() + "/" + info.getY() + "/" + info.getZ();
	}

	@Override
	public void setActive(Portal portal, boolean active) {
		config.set("portals." + portal.getTag() + ".enable", active);
		IGates.getPlugin().saveConfig();
		
		portal.setActive(active);
	}
        
        
        @Override
        public void setBungee(Portal portal, boolean bungee) {
                config.set("portals." + portal.getTag() + ".bungee", bungee);
                IGates.getPlugin().saveConfig();

                portal.setBungee(bungee);
        }

	@Override
	public void setPrice(Portal p, int price)
	{
		config.set("portals." + p.getTag() + ".price", price);
		IGates.getPlugin().saveConfig();
	}

	@Override
	public void setSpawn(Portal portal, Location l) {
		config.set("portals." + portal.getTag() + ".to", this.convertLocationToString(l));
		config.set("portals." + portal.getTag() + ".yaw", l.getYaw());
		config.set("portals." + portal.getTag() + ".pitch", l.getPitch());
		IGates.getPlugin().saveConfig();
		
		portal.setToPoint(l);
	}
        
        @Override
	public void setServer(Portal portal, String server) {
		config.set("portals." + portal.getTag() + ".server", server);
		IGates.getPlugin().saveConfig();
		
		portal.setServer(server);
	}

	@Override
	public void setFillType(Portal portal, FillType filltype) {
		config.set("portals." + portal.getTag() + ".filltype", filltype.getName());
		IGates.getPlugin().saveConfig();
		
		portal.setFillType(filltype);
	}

	@Override
	public void setFromsAreas(Portal portal, ArrayList<Volume> froms) {
		ArrayList<HashMap<String, Object>> fin = new ArrayList<HashMap<String, Object>>();
		for (Volume v : froms)
		{
			HashMap<String, Object> to = new HashMap<String, Object>();
			to.put("begin", convertLocationToString(v.getFirst()));
			to.put("end", convertLocationToString(v.getEnd()));
			fin.add(to);
		}
		config.set("portals." + portal.getTag() + ".froms", fin);
		IGates.getPlugin().saveConfig();
		
		portal.setFromPoints(froms);
	}

	
}
