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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public class Portals extends JavaPlugin implements Listener {

    private class PortalCheck implements Runnable {
        @Override
        public void run() {
            final Iterator<PortalPlayer> iterator = Portals.this.players.iterator();
            while (iterator.hasNext()) {
                final PortalPlayer player = iterator.next();
                if (player.check()) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    private final HashSet<PortalArea> portalAreas = new HashSet<PortalArea>();
    private final HashSet<PortalPlayer> players = new HashSet<PortalPlayer>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((args.length == 1) && args[0].equalsIgnoreCase("reload")) {
            this.loadPortalAreas();
            sender.sendMessage(ChatColor.AQUA + "Portals reloaded");
        }
        return true;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Who's gonna make the cake when I'm gone? You?");
    }

    @Override
    public void onEnable() {
        if (!(new File(this.getDataFolder(), "config.yml")).exists()) {
            this.saveDefaultConfig();
        }
        if (!(new File(this.getDataFolder(), "portals.yml")).exists()) {
            this.saveDefaultPortals();
        }
        if (this.getConfig().getBoolean("meow", true)) {
            this.getLogger().info("            |\\=/|");
            this.getLogger().info("        _   /6 6\\ ");
            this.getLogger().info("        )) =\\_Y_/=        ,,,,,      |\\=/|.-\"\"\"-.");
            this.getLogger().info("       ((   / ^ \\        _|||||_     /6 6\\       \\");
            this.getLogger().info("        \\\\ /| | |\\      {~*~*~*~}   =\\_Y_/=  (_  ;\\");
            this.getLogger().info("         \\( | | | )   __{*~*~*~*}__    ^//_/-/__///");
            this.getLogger().info("           `\"\" \"\"`   `-------------`            ((");
        }
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "RubberBand");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new PortalCheck(), 4, 4);
        for (final Player player : this.getServer().getOnlinePlayers()) {
            this.addPlayer(player);
        }
        this.loadPortalAreas();
        this.getLogger().info("[Portals] There's a hole in the sky, through which things can fly!");
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        this.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        final Iterator<PortalPlayer> iterator = this.players.iterator();
        while (iterator.hasNext()) {
            final PortalPlayer player = iterator.next();
            if (player.getName().equals(event.getPlayer().getName())) {
                iterator.remove();
                return;
            }
        }
    }

    private void addPlayer(Player player) {
        this.players.add(new PortalPlayer(player, this.getPortalForPlayer(player) != null, this));
    }

    private void loadPortalAreas() {
        this.getDataFolder().mkdirs();
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "portals.yml"));
        this.portalAreas.clear();
        for (final String name : config.getKeys(false)) {
            final ConfigurationSection portal = config.getConfigurationSection(name);
            final int baseX = portal.getInt("x", 0);
            final int baseY = portal.getInt("y", 0);
            final int baseZ = portal.getInt("z", 0);
            final boolean xdim = portal.getBoolean("horizontalByX", false);
            final String worldName = portal.getString("world", "world");
            World world = this.getServer().getWorld(worldName);
            if (world == null) {
                world = this.getServer().getWorlds().get(0);
            }
            final HashSet<Location> locations = new HashSet<Location>();
            final List<String> shape = Arrays.asList(portal.getString("shape").split("\n"));
            int curX = baseX;
            int curY = baseY + shape.size();
            int curZ = baseZ;
            for (final String line : shape) {
                curY--;
                if (xdim) {
                    curX = baseX;
                } else {
                    curZ = baseZ;
                }
                for (final char c : line.toCharArray()) {
                    if (c == 'O') {
                        final Location loc = new Location(world, curX, curY, curZ);
                        locations.add(loc);
                    }
                    if (xdim) {
                        curX++;
                    } else {
                        curZ++;
                    }
                }
            }

            final String perm = portal.getString("permission");
            if (!perm.equals("portals.everyone")) {
                try {
                    this.getServer().getPluginManager().addPermission(new Permission(perm, PermissionDefault.OP));
                } catch (final IllegalArgumentException e) {
                    // Oh well!
                }
            }

            final String target = portal.getString("target");

            this.portalAreas.add(new PortalArea(locations, name, target, perm));
        }
    }

    private void saveDefaultPortals() {
        this.saveResource("portals.yml", false);
    }

    PortalArea getPortalForPlayer(Player player) {
        for (final PortalArea area : this.portalAreas) {
            if (area.isPlayerInPortal(player)) {
                return area;
            }
        }
        return null;
    }
}
