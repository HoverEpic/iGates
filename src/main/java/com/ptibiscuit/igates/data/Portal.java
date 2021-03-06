/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptibiscuit.igates.data;

import com.ptibiscuit.igates.IGates;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author ANNA
 */
public class Portal
{

    private String tag;
    private Location toPoint;
    private String server;
    private ArrayList<Volume> fromPoints;
    private FillType fillType;
    private int price;
    private boolean active;
    private boolean bungee;

    public Portal(String tag, Location toPoint, String server, ArrayList<Volume> fromPoints, int price, FillType fillType, boolean active, boolean bungee)
    {
        this.tag = tag;
        this.toPoint = toPoint;
        this.server = server;
        this.price = price;
        this.fromPoints = fromPoints;
        this.fillType = fillType;
        this.active = active;
        this.bungee = bungee;
    }

    public FillType getFillType()
    {
        return fillType;
    }

    public int getPrice()
    {
        return price;
    }

    public ArrayList<Volume> getFromPoints()
    {
        return fromPoints;
    }

    public String getTag()
    {
        return tag;
    }

    public boolean isActive()
    {
        return active;
    }

    public boolean isBungee()
    {
        return bungee;
    }

    public boolean teleportPlayer(Player p)
    {
        // We check for the price
        if (IGates.getPlugin().isEconomyEnabled() && this.price != 0 && !p.hasPermission("god")) {
            Economy econ = IGates.getPlugin().getEconomy();
            double actualMoneyOfPlayer = econ.getBalance(p.getName());
            String formatPrice = econ.format(this.price);
            if (actualMoneyOfPlayer >= this.price) {
                IGates.getPlugin().sendTranslation(p, IGates.getPlugin().getTranslation("pay_the_price").replace("{PRICE}", formatPrice));
                econ.withdrawPlayer(p.getName(), this.price);
            }
            else {
                // Il n'a pas assez d'argent
                IGates.getPlugin().sendTranslation(p, IGates.getPlugin().getTranslation("cant_afford").replace("{PRICE}", formatPrice));
                return false;
            }
        }
        Location l = this.toPoint;
        Chunk c = this.toPoint.getChunk();
        if (!l.getWorld().isChunkLoaded(c)) {
            l.getWorld().loadChunk(c);
        }
        p.teleport(l);
        return true;
    }

    public boolean sendPlayerToServer(Player p)
    {
        // We check for the price
        if (IGates.getPlugin().isEconomyEnabled() && this.price != 0 && !p.hasPermission("god")) {
            Economy econ = IGates.getPlugin().getEconomy();
            double actualMoneyOfPlayer = econ.getBalance(p.getName());
            String formatPrice = econ.format(this.price);
            if (actualMoneyOfPlayer >= this.price) {
                IGates.getPlugin().sendTranslation(p, IGates.getPlugin().getTranslation("pay_the_price").replace("{PRICE}", formatPrice));
                econ.withdrawPlayer(p.getName(), this.price);
            }
            else {
                // Il n'a pas assez d'argent
                IGates.getPlugin().sendTranslation(p, IGates.getPlugin().getTranslation("cant_afford").replace("{PRICE}", formatPrice));
                return false;
            }
        }
        if (IGates.getPlugin().getConfig().getBoolean("config.InventorySQL_support")) {
            // ici action de téléport server
            Bukkit.getServer().getLogger().log(Level.INFO, "Player {0} teleported to server {1}.", new Object[]{p.getDisplayName(), this.server});
            p.sendMessage("Teleport to server " + this.server + ".");
            ServerChanger.changeServerTarget(this.server, p);
        }
        else {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("Connect");
                out.writeUTF(server);
            } catch (IOException e) {
            }
            Bukkit.getServer().getLogger().log(Level.INFO, "Player {0} teleported to server {1}.", new Object[]{p.getDisplayName(), this.server});
            p.sendMessage("Teleport to server " + this.server + ".");
            p.sendPluginMessage(IGates.getPlugin(), IGates.CHANNEL, b.toByteArray());
        }

        return true;
    }

    public boolean isIn(Location l, double offset)
    {
        for (Volume v : this.fromPoints) {
            if (v.isIn(l, offset)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIn(Location l)
    {
        return this.isIn(l, 0);
    }

    public void setActive(boolean active)
    {
        this.active = active;
        if (active) {
            this.fillBlocks();
        }
        else {
            this.defillBlocks();
        }
    }

    public void setBungee(boolean bungee)
    {
        this.bungee = bungee;
    }

    public void setFillType(FillType fillType)
    {
        if (this.isActive()) {
            this.defillBlocks();
            this.fillType = fillType;
            this.fillBlocks();
        }
    }

    public void beforeDelete()
    {
        this.defillBlocks();
    }

    public void defillBlocks()
    {
        for (Volume v : this.fromPoints) {
            this.fillType.defillBlocks(v.getBlocks());
        }
    }

    public void fillBlocks()
    {
        for (Volume v : this.fromPoints) {
            this.fillType.fillBlocks(v.getBlocks());
        }
    }

    public void setFromPoints(ArrayList<Volume> fromPoints)
    {
        this.fromPoints = fromPoints;
    }

    public void setToPoint(Location toPoint)
    {
        this.toPoint = toPoint;
    }

    public void setServer(String server)
    {
        this.server = server;
    }
}
