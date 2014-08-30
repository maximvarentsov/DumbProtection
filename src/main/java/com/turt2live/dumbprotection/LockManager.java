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

import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockManager {

    private FileConfiguration file;
    private File storeFile;

    public LockManager() throws IOException, InvalidConfigurationException {
        storeFile = new File(DumbProtection.getInstance().getDataFolder(), "protections.yml");
        file = new YamlConfiguration();

        if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
        if (!storeFile.exists()) storeFile.createNewFile();

        file.load(storeFile);
    }

    public void save() throws IOException {
        file.save(storeFile);
    }

    public Protection getProtection(Block location) {
        if (location == null) throw new IllegalArgumentException();
        String id = location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName();
        if (file.contains(id)) {
            return new Protection(this, id);
        }
        return null;
    }

    public Protection add(Block location, UUID owner) {
        if (location == null || owner == null) throw new IllegalArgumentException();
        String id = location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName();
        file.set(id + ".owner", owner.toString());

        return new Protection(this, id);
    }

    UUID owner(String id) {
        return UUID.fromString(file.getString(id + ".owner"));
    }

    void add(String id, UUID player) {
        List<String> players = file.getStringList(id + ".players");
        if (players == null) players = new ArrayList<String>();

        players.add(player.toString());
        file.set(id + ".players", players);
    }

    void remove(String id, UUID player) {
        List<String> players = file.getStringList(id + ".players");
        if (players == null) players = new ArrayList<String>();

        players.remove(player.toString());
        file.set(id + ".players", players);
    }

    List<UUID> list(String id) {
        List<String> players = file.getStringList(id + ".players");
        if (players == null) players = new ArrayList<String>();

        List<UUID> uuids = new ArrayList<UUID>();

        for (String player : players) {
            uuids.add(UUID.fromString(player));
        }

        return uuids;
    }

    void delete(String id) {
        file.set(id, null);
    }

}
