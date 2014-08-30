/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.turt2live.dumbprotection;

import com.turt2live.commonsense.DumbPlugin;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DumbProtection extends DumbPlugin {

    private static DumbProtection instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        //initCommonSense(72122);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, ChatColor.RED + "You must be a player to use the commands.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Block target = player.getTargetBlock(null, 6);
            if (target == null) sendMessage(sender, ChatColor.RED + "You do not have a target block!");
            else {
                List<String> valid = getConfig().getStringList("valid-blocks");
                if (sender.hasPermission("protect.lock") && valid != null && valid.contains(target.getType().name())) {
                    // TODO
                } else sendMessage(sender, ChatColor.RED + "You cannot protect that block!");
            }
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length < 2) {
                    sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/protect add <player>");
                    return true;
                }
                // TODO
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) {
                    sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/protect remove <player>");
                    return true;
                }
                // TODO
            } else if (args[0].equalsIgnoreCase("list")) {
                // TODO
            } else if (args[0].equalsIgnoreCase("help")) {
                sendMessage(sender, ChatColor.AQUA + "/protect lock" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Locks your current targeted block to you");
                sendMessage(sender, ChatColor.AQUA + "/protect add <player>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Adds a player to the targeted protection");
                sendMessage(sender, ChatColor.AQUA + "/protect remove <player>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Removes a player from the targeted protection");
                sendMessage(sender, ChatColor.AQUA + "/protect list" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Lists all the players on the targeted protection");
            } else {
                sendMessage(sender, ChatColor.RED + "Unknown command. Try " + ChatColor.YELLOW + "/protect help");
            }
        }
        return true;
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage((ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix", ChatColor.GRAY + "[DumbCoin]")) + " " + ChatColor.WHITE + message).trim());
    }

    public static DumbProtection getInstance() {
        return instance;
    }
}
