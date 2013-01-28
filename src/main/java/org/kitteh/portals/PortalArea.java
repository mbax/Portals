/*
 * * Copyright (C) 2012 Aksel Slettemark and Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.portals;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PortalArea {

    private final HashSet<Location> locations;
    private final byte[] destination;
    private final String permission;
    private final String name;

    PortalArea(HashSet<Location> locations, String name, String destination, String permission) {
        this.locations = locations;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(12);
        DataOutputStream stream = new DataOutputStream(bytes);
        try {
            stream.writeUTF("Connect");
            stream.writeUTF(destination);
        } catch (IOException e) {
        }
        this.destination = bytes.toByteArray();
        this.permission = permission;
        this.name = name;
    }

    byte[] getDestination() {
        return this.destination;
    }

    HashSet<Location> getLocations() {
        return this.locations;
    }

    String getName() {
        return this.name;
    }

    String getPermission() {
        return this.permission;
    }

    boolean isLocationInPortal(Location loc) {
        return this.locations.contains(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    boolean isPlayerInPortal(Player player) {
        final Location loc = player.getLocation();
        final Location test = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (this.locations.contains(test)) {
            return true;
        }
        test.add(0, 1, 0);
        return this.locations.contains(test);
    }

}
