/*
 * * Copyright (C) 2014 Matt Baxter http://kitteh.org
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
package org.kitteh.tenjava.jul2014;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public final class WonderCommand implements TabExecutor {
    private final WonderBow plugin;

    WonderCommand(WonderBow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                switch (args[0]) {
                    case "craft":
                        if (!sender.hasPermission("wonderbow.command.craft")) {
                            sender.sendMessage(ChatColor.RED + "Access denied");
                            return true;
                        }
                        player.getInventory().addItem(this.plugin.getNewWonderBow(player.getName()));
                        player.getInventory().addItem(this.plugin.getNewWonderBro());
                        sender.sendMessage(ChatColor.AQUA + "WonderBow craft!");
                        break;
                }
            } else {
                if (sender.hasPermission("wonderbow.command.craft")) {
                    sender.sendMessage(ChatColor.RED + "Available commands: craft");
                } else {
                    sender.sendMessage(ChatColor.RED + "You cannot into WonderBow");
                }
            }
        } else {
            sender.sendMessage("WonderBow commands only work for players");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) {
            return Arrays.asList("craft");
        }
        return null;
    }
}