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

import java.util.List;
import java.util.UUID;

public class Protection {

    private LockManager manager;
    private String id;

    Protection(LockManager manager, String protectionId) {
        this.manager = manager;
        this.id = protectionId;
    }

    public UUID getOwner() {
        return manager.owner(id);
    }

    public List<UUID> getPlayers() {
        return manager.list(id);
    }

    public void addPlayer(UUID player) {
        if (player == null) throw new IllegalArgumentException();
        manager.add(id, player);
    }

    public void removePlayer(UUID player) {
        if (player == null) throw new IllegalArgumentException();
        manager.remove(id, player);
    }

    public void delete() {
        manager.delete(id);
    }

}
