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
import com.turt2live.uuid.CachingServiceProvider;
import com.turt2live.uuid.PlayerRecord;
import com.turt2live.uuid.ServiceProvider;
import com.turt2live.uuid.turt2live.v2.ApiV2Service;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class DumbProtection extends DumbPlugin {

    private static DumbProtection instance;
    private LockManager manager;
    private ServiceProvider uuidProvider;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        initCommonSense(84600);

        uuidProvider = new CachingServiceProvider(new ApiV2Service());

        try {
            manager = new LockManager();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new ProtectionListener(manager), this);
    }

    @Override
    public void onDisable() {
        instance = null;
        save();
    }

    private void save() {
        try {
            manager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, ChatColor.RED + "You must be a player to use the commands.");
            return true;
        }

        Player player = (Player) sender;
        Block target = player.getTargetBlock(null, 6);
        if (args.length < 1 || !args[0].equalsIgnoreCase("help"))
            if (target == null || target.getType() == Material.AIR) {
                sendMessage(sender, ChatColor.RED + "You do not have a target block!");
                return true;
            }
        Protection existing = null;
        if (target != null) existing = manager.getProtection(target);

        if (args.length == 0) {
            List<String> valid = getConfig().getStringList("valid-blocks");
            if (sender.hasPermission("protect.lock") && valid != null && valid.contains(target.getType().name()) && existing == null) {
                manager.add(target, player.getUniqueId());
                save();
                sendMessage(sender, ChatColor.GREEN + "You have protected that block");
            } else sendMessage(sender, ChatColor.RED + "You cannot protect that block!");
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length < 2) {
                    sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/protect add <player>");
                    return true;
                }

                if (!player.hasPermission("protect.add")) {
                    sendMessage(sender, ChatColor.RED + "You do not have permission to do that!");
                    return true;
                }

                if (existing == null || (!existing.getOwner().equals(player.getUniqueId()) && !player.hasPermission("protect.add.others"))) {
                    sendMessage(sender, ChatColor.RED + "There is no protection there or you do not have permission to edit it.");
                } else {
                    PlayerRecord suppliedPlayer = uuidProvider.doLookup(args[1]);
                    if (suppliedPlayer == null || suppliedPlayer.getUuid() == null) {
                        sendMessage(sender, ChatColor.RED + "Player not found!");
                    } else {
                        existing.addPlayer(suppliedPlayer.getUuid());
                        sendMessage(sender, ChatColor.GREEN + "Player added!");
                        save();
                    }
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) {
                    sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/protect remove <player>");
                    return true;
                }

                if (!player.hasPermission("protect.remove")) {
                    sendMessage(sender, ChatColor.RED + "You do not have permission to do that!");
                    return true;
                }

                if (existing == null || (!existing.getOwner().equals(player.getUniqueId()) && !player.hasPermission("protect.remove.others"))) {
                    sendMessage(sender, ChatColor.RED + "There is no protection there or you do not have permission to edit it.");
                } else {
                    PlayerRecord suppliedPlayer = uuidProvider.doLookup(args[1]);
                    if (suppliedPlayer == null || suppliedPlayer.getUuid() == null) {
                        sendMessage(sender, ChatColor.RED + "Player not found!");
                    } else {
                        existing.removePlayer(suppliedPlayer.getUuid());
                        sendMessage(sender, ChatColor.GREEN + "Player removed!");
                        save();
                    }
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                if (!player.hasPermission("protect.list")) {
                    sendMessage(sender, ChatColor.RED + "You do not have permission to do that!");
                    return true;
                }

                if (existing == null || (!existing.getOwner().equals(player.getUniqueId()) && !existing.getPlayers().contains(player.getUniqueId()) && !player.hasPermission("protect.list.others"))) {
                    sendMessage(sender, ChatColor.RED + "There is no protection there or you do not have permission to view it.");
                } else {
                    List<UUID> players = existing.getPlayers();
                    sendMessage(sender, ChatColor.AQUA + "Owner: " + ChatColor.YELLOW + uuidProvider.doLookup(existing.getOwner()).getName());
                    sendMessage(sender, ChatColor.AQUA + "Allowed Players:");
                    for (UUID uuid : players) {
                        sendMessage(sender, ChatColor.YELLOW + "  " + uuidProvider.doLookup(uuid).getName());
                    }
                    if (players.size() == 0)
                        sendMessage(sender, ChatColor.YELLOW + "" + ChatColor.ITALIC + "  No one");
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (!player.hasPermission("protect.delete")) {
                    sendMessage(sender, ChatColor.RED + "You do not have permission to do that!");
                    return true;
                }

                if (existing == null || (!existing.getOwner().equals(player.getUniqueId()) && !player.hasPermission("protect.delete.others"))) {
                    sendMessage(sender, ChatColor.RED + "There is no protection there or you do not have permission to view it.");
                } else {
                    existing.delete();
                    save();
                    sendMessage(sender, ChatColor.GREEN + "Protection removed!");
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                sendMessage(sender, ChatColor.AQUA + "/protect lock" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Locks your current targeted block to you");
                sendMessage(sender, ChatColor.AQUA + "/protect add <player>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Adds a player to the targeted protection");
                sendMessage(sender, ChatColor.AQUA + "/protect remove <player>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Removes a player from the targeted protection");
                sendMessage(sender, ChatColor.AQUA + "/protect delete" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Deletes the targeted protection");
                sendMessage(sender, ChatColor.AQUA + "/protect list" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Lists all the players on the targeted protection");
            } else {
                sendMessage(sender, ChatColor.RED + "Unknown command. Try " + ChatColor.YELLOW + "/protect help");
            }
        }
        return true;
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage((ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix", ChatColor.GRAY + "[DumbProtection]")) + " " + ChatColor.WHITE + message).trim());
    }

    public static DumbProtection getInstance() {
        return instance;
    }
}
