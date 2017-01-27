package com.ptibiscuit.igates;

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
import java.util.Map;
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
import org.bukkit.plugin.java.JavaPlugin;

public class IGates extends JavaPlugin implements Listener
{

    private static IGates m_Plugin;
    public static final String CHANNEL = "BungeeCord";
    private final Map<String, String> m_Translations = new HashMap();

    private IData m_IData;
    private VolumeSelectionManager m_VolumeSelectionManager = new VolumeSelectionManager();
    private SpreadBlockListener m_SpreadBlockListener = new SpreadBlockListener();
    private PlayerListener m_PlayerListener = new PlayerListener();
    private Economy m_Economy;

    @Override
    public void onLoad()
    {
        m_Plugin = this;
        FileConfiguration config = getConfig();
        if (config.contains("portals")) {
            config.set("portals", new HashMap());
        }
        if (config.contains("config.retain_liquid")) {
            config.set("config.retain_liquid", false);
        }
        if (config.contains("config.InventorySQL_support")) {
            config.set("config.InventorySQL_support", false);
        }
        if (config.contains("config.display_message_selection")) {
            config.set("config.display_message_selection", true);
        }
        saveConfig();
    }

    @Override
    public void onEnable()
    {
        getLogger().info("");
        getLogger().info("iGates by Ptibiscuit");
        getLogger().info("Modified for Hovercarft");
        getLogger().info("");

        this.m_IData = new YamlData();
        // On fait attention à Multiverse, au cas où.
        m_IData.loadPortals();
        getLogger().info(m_IData.getPortals().size() + " portals loaded !");
        // Enable Economic support
        if (this.setupEconomy()) {
            getLogger().info("Economy enabled !");
        }
        else {
            getLogger().info("Can't enable Economy. It wasn't necessary, but it's sad ... :'(");
        }
        PluginManager pgm = this.getServer().getPluginManager();
        pgm.registerEvents(m_VolumeSelectionManager, this);
        pgm.registerEvents(m_PlayerListener, this);
        pgm.registerEvents(m_SpreadBlockListener, this);
        pgm.registerEvents(this, this);

        //register BungeeCord chanel to send server command
        getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
    }

    public void loadLanguages()
    {
        m_Translations.put("cant_do", "You're not able to do that.");
        m_Translations.put("need_be_player", "You need to be a player to do that.");
        m_Translations.put("more_args", "Bad using of the command (Need argument ?).");
        m_Translations.put("tag_taken", "This portal's tag ios already taken.");
        m_Translations.put("ft_dont_exist", "This FillType doesn't exist (water, portal, end_portal, lava, web)");
        m_Translations.put("tag_dont_exist", "This portal's tag doesn't exist.");
        m_Translations.put("set_active", "This portal has been turned {ACTIVE}.");
        m_Translations.put("set_bungee", "This portal is now bungee ({SWITCH}).");
        m_Translations.put("set_filltype", "This portal's filltype is now {FILLTYPE}.");
        m_Translations.put("set_to", "The new \"to\" point of the portal is set.");
        m_Translations.put("set_server", "The new \"to\" server of the portal is set.");
        m_Translations.put("portal_deleted", "Portal deleted. :'(");
        m_Translations.put("first_point_set", "The first point of your selection is set !");
        m_Translations.put("second_point_set", "The second point of your selection is set !");
        m_Translations.put("froms_added", "\"From area\" added to the portal !");
        m_Translations.put("need_volume", "Before do that, you need to select an area with the woodaxe, like with WorldEdit. =)");
        m_Translations.put("top_list", "List of all portals:");
        m_Translations.put("elem_list", "{ACTIVE} " + ChatColor.GOLD + "{TAG}" + ChatColor.WHITE + ": {CNT_FROMS} \"Froms\" areas.");
        m_Translations.put("portail_created", "Portal \"{TAG}\" created !");
        m_Translations.put("weird_arg", "You used weird arg, only on, off, to and filltype are available.");
        m_Translations.put("cant_afford", "You don't have enough money to take this portal. It costs {PRICE}.");
        m_Translations.put("pay_the_price", "You have paid {PRICE} to take this portal.");
        m_Translations.put("set_price", "The new price of the portal is set !");
    }

    public String getTranslation(String p_Key)
    {
        return m_Translations.get(p_Key);
    }

    public void sendTranslation(CommandSender p_CommandSender, String p_Key)
    {
        p_CommandSender.sendMessage(getTranslation(p_Key));
    }

    @Override
    public void onDisable()
    {

    }

    public boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.m_Economy = rsp.getProvider();
        return m_Economy != null;
    }

    public Economy getEconomy()
    {
        return this.m_Economy;
    }

    public boolean isEconomyEnabled()
    {
        return this.m_Economy != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        try {
            if (!(sender instanceof Player)) {
                sendTranslation(sender, "need_be_player");
                return true;
            }
            Player p = (Player) sender;

            if (label.equalsIgnoreCase("igcreate")) {
                if (!sender.hasPermission("portal.edit.create")) {
                    sendTranslation(sender, "cant_do");
                    return true;
                }

                if (this.getPortal(args[0]) == null) {
                    FillType ft = FillType.getFillType(args[1]);
                    if (ft != null) {
                        m_IData.createPortal(args[0], p.getLocation(), new ArrayList<Volume>(), ft);
                        sender.sendMessage(getTranslation("portail_created").replace("{TAG}", args[0]));
                    }
                    else {
                        sendTranslation(sender, "ft_dont_exist");
                    }
                }
                else {
                    sendTranslation(sender, "tag_taken");
                }
            }
            else if (label.equalsIgnoreCase("igset")) {
                if (!sender.hasPermission("portal.edit.set." + args[0])) {
                    sendTranslation(sender, "cant_do");
                    return true;
                }

                Portal portal = this.getPortal(args[1]);
                if (portal == null) {
                    sendTranslation(sender, "tag_dont_exist");
                    return true;
                }

                if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                    boolean active = (args[0].equalsIgnoreCase("on")) ? true : false;
                    this.m_IData.setActive(portal, active);
                    sender.sendMessage(getTranslation("set_active").replace("{ACTIVE}", args[0]));
                }
                else if (args[0].equalsIgnoreCase("bungee")) {
                    boolean bungee = (args[2].equalsIgnoreCase("on")) ? true : false;
                    this.m_IData.setBungee(portal, bungee);
                    sender.sendMessage(getTranslation("set_bungee").replace("{SWITCH}", args[2]));
                }
                else if (args[0].equalsIgnoreCase("price")) {
                    int price = new Integer(args[2]);
                    this.m_IData.setPrice(portal, price);
                    sendTranslation(sender, "set_price");
                }
                else if (args[0].equalsIgnoreCase("to")) {
                    this.m_IData.setSpawn(portal, p.getLocation());
                    sender.sendMessage(getTranslation("set_to"));
                }
                else if (args[0].equalsIgnoreCase("server")) {
                    this.m_IData.setServer(portal, args[2]);
                    sender.sendMessage(getTranslation("set_server"));
                }
                else if (args[0].equalsIgnoreCase("filltype")) {
                    FillType ft = FillType.getFillType(args[2]);
                    if (ft != null) {
                        this.m_IData.setFillType(portal, ft);
                        sender.sendMessage(getTranslation("set_filltype").replace("{FILLTYPE}", args[2]));
                    }
                    else {
                        sendTranslation(sender, "ft_dont_exist");
                    }
                }
                else {
                    sendTranslation(sender, "weird_arg");
                }
            }
            else if (label.equalsIgnoreCase("igdelete")) {
                if (!sender.hasPermission("portal.edit.delete")) {
                    sendTranslation(sender, "cant_do");
                    return true;
                }

                Portal portal = this.getPortal(args[0]);
                if (portal == null) {
                    sendTranslation(sender, "tag_dont_exist");
                    return true;
                }

                portal.beforeDelete();
                this.m_IData.deletePortal(portal);
                sendTranslation(sender, "portal_deleted");
            }
            else if (label.equalsIgnoreCase("iglist")) {
                if (!sender.hasPermission("portal.list")) {
                    sendTranslation(sender, "cant_do");
                    return true;
                }

                sendTranslation(sender, "top_list");
                for (Portal portal : this.m_IData.getPortals()) {
                    String enable = (portal.isActive()) ? ChatColor.GREEN + "[V]" : ChatColor.RED + "[X]";
                    enable += ChatColor.WHITE;
                    int cntFroms = portal.getFromPoints().size();
                    sender.sendMessage(getTranslation("elem_list").replace("{TAG}", portal.getTag()).replace("{ACTIVE}", enable).replace("{CNT_FROMS}", String.valueOf(cntFroms)));
                }
            }
            else if (label.equalsIgnoreCase("igaddfrom")) {
                if (!sender.hasPermission("portal.edit.addfrom")) {
                    sendTranslation(sender, "cant_do");
                    return true;
                }

                Portal portal = this.getPortal(args[0]);
                if (portal == null) {
                    sendTranslation(sender, "tag_dont_exist");
                    return true;
                }
                Volume v = this.m_VolumeSelectionManager.getSelection(p.getName());
                if (v != null) {
                    portal.getFromPoints().add(v);
                    this.m_IData.setFromsAreas(portal, portal.getFromPoints());
                    this.m_VolumeSelectionManager.removeSelection(p.getName());
                    sendTranslation(sender, "froms_added");
                }
                else {
                    sendTranslation(sender, "need_volume");
                }
            }
            else if (label.equalsIgnoreCase("igclearfroms")) {
                if (!sender.hasPermission("portal.edit.clearfroms")) {
                    sendTranslation(sender, "cant_do");
                    return true;
                }

                Portal portal = this.getPortal(args[0]);
                if (portal == null) {
                    sendTranslation(sender, "tag_dont_exist");
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            sendTranslation(sender, "more_args");
        }
        return true;
    }

    public Portal getPortalByPosition(Location l, double offset)
    {
        for (Portal p : this.m_IData.getPortals()) {
            if (p.isIn(l, offset) && p.isActive()) {
                return p;
            }
        }
        return null;
    }

    public Portal getPortalByPosition(Location l)
    {
        return this.getPortalByPosition(l, 0);
    }

    public Portal getPortal(String tag)
    {
        for (Portal p : this.m_IData.getPortals()) {
            if (p.getTag().equalsIgnoreCase(tag)) {
                return p;
            }
        }
        return null;
    }

    public IData getData()
    {
        return m_IData;
    }

    /**
     * @return the m_Plugin
     */
    public static IGates getPlugin()
    {
        return m_Plugin;
    }
}
