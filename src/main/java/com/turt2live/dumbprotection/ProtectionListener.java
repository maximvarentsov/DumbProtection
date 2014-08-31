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

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionListener implements Listener {

    private LockManager manager;

    ProtectionListener(LockManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Protection existing = manager.getProtection(block);
        if (!isAllowedToDelete(existing, player)) {
            event.setCancelled(true);
            DumbProtection.getInstance().sendMessage(player, ChatColor.RED + "You cannot delete that protection!");
        } else if (existing != null) {
            existing.delete();
            DumbProtection.getInstance().sendMessage(player, ChatColor.GREEN + "Protection removed.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && block!=null){
            Protection protection = manager.getProtection(block);
            if(!isAllowedToUse(protection,player)){
                DumbProtection.getInstance().sendMessage(player,ChatColor.RED+"You cannot use that block.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRedstoneChange(BlockRedstoneEvent event){
        Block block = event.getBlock();
        Protection protection = manager.getProtection(block);
        if(protection != null){
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    private boolean isAllowedToUse(Protection protection, Player player) {
        return player.hasPermission("protect.bypass") || protection == null
                || protection.getOwner().equals(player.getUniqueId()) || protection.getPlayers().contains(player.getUniqueId());
    }

    private boolean isAllowedToDelete(Protection protection, Player player) {
        return player.hasPermission("protect.delete.others") || protection == null
                || protection.getOwner().equals(player.getUniqueId());
    }
}
