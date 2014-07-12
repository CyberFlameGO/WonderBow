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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

class WonderListener implements Listener {
    private static final String WONDERMETA = "WonderBow";
    private final MetadataValue metavalue;
    private final WonderBow plugin;

    WonderListener(WonderBow plugin) {
        this.plugin = plugin;
        this.metavalue = new FixedMetadataValue(this.plugin, WONDERMETA);
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && this.plugin.isWonderBow(((Player) event.getEntity()).getItemInHand())) {
            this.plugin.getServer().broadcastMessage("SHOT");
            event.getProjectile().setMetadata(WONDERMETA, this.metavalue);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity().getMetadata(WONDERMETA).contains(this.metavalue)) {
            this.plugin.getServer().broadcastMessage("HIT");
        }
    }
}