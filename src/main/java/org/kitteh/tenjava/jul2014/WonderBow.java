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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tenjava.jul2014.effects.ParticleTimer;

import java.util.Arrays;
import java.util.List;

public class WonderBow extends JavaPlugin {
    private ItemMeta metaBow;
    private ParticleTimer particles;

    @Override
    public void onEnable() {
        this.getCommand("wonderbow").setExecutor(new WonderCommand(this));
        this.getServer().getPluginManager().registerEvents(new WonderListener(this), this);
        this.particles = new ParticleTimer(this);

        // Create the WonderBow meta. This will be consistent and required.
        this.metaBow = this.getServer().getItemFactory().getItemMeta(Material.BOW);
        this.metaBow.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "WonderBow!");
        this.metaBow.setLore(Arrays.asList(ChatColor.DARK_AQUA + "It's a WonderBow!", "", ChatColor.DARK_AQUA + "Its effects are a mystery"));
        this.metaBow.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
    }

    /**
     * Creates a new WonderBow ItemMeta for a specified crafter.
     *
     * @param creator creator of this bow
     * @return fresh ItemMeta out of the oven
     */
    private ItemMeta getNewMeta(String creator) {
        ItemMeta meta = this.metaBow.clone();
        List<String> lore = this.metaBow.getLore();
        lore.addAll(Arrays.asList("", ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "Crafted by " + ChatColor.BOLD + creator));
        meta.setLore(lore);
        return meta;
    }

    /**
     * Creates a new WonderBow!
     *
     * @param creator the dude who made this
     * @return a new WonderBow
     */
    public ItemStack getNewWonderBow(String creator) {
        ItemStack bow = new ItemStack(Material.BOW);
        bow.setItemMeta(this.getNewMeta(creator));
        return bow;
    }

    /**
     * Gets the particle timer.
     *
     * @return particle timer
     */
    public ParticleTimer getParticleTimer() {
        return this.particles;
    }

    /**
     * Determines if a given item is a WonderBow.
     *
     * @param item potential bow
     * @return true if a WonderBow
     */
    public boolean isWonderBow(ItemStack item) {
        if (item != null && item.getType() == Material.BOW) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getDisplayName().equals(this.metaBow.getDisplayName())) {
                List<String> lore = meta.getLore();
                if (lore.size() > this.metaBow.getLore().size()) {
                    return lore.subList(0, this.metaBow.getLore().size()).equals(this.metaBow.getLore());
                }
            }
        }
        return false;
    }
}