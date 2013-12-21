/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptibiscuit.igates.data;

import com.ptibiscuit.igates.Plugin;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.entity.Player;
import tk.manf.InventorySQL.manager.DatabaseManager;
import tk.manf.InventorySQL.manager.InventoryLockingSystem;

/**
 *
 * @author Vincent
 */
public class ServerChanger {

    private static final String CHANNEL = "BungeeCord";

    public static void changeServerTarget(String server, Player target) {

        InventoryLockingSystem.getInstance().addLock(target.getName());
        DatabaseManager.getInstance().savePlayer(target);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {
        }

        target.sendPluginMessage(Plugin.instance, CHANNEL, b.toByteArray());
        InventoryLockingSystem.getInstance().removeLock(target.getName());
    }
}
