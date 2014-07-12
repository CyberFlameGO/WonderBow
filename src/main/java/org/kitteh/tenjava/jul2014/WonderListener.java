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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

class WonderListener implements Listener {
    private static final String WONDERMETA = "WonderBow";
    private static final String WONDERSHOOTER = "WonderShooter";
    private final WonderBow plugin;

    WonderListener(WonderBow plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && this.plugin.isWonderBow(((Player) event.getEntity()).getItemInHand())) {
            Wonder wonder = Wonder.getWonder();
            Entity projectile = event.getProjectile();
            if (!wonder.getEntityClass().isAssignableFrom(projectile.getClass())) {
                event.setCancelled(true);
                projectile = this.spawnWonder(wonder, projectile, (Player) event.getEntity());
            }
            wonder.onSpawn(projectile);
            projectile.setMetadata(WONDERMETA, new FixedMetadataValue(this.plugin, wonder));
            projectile.setMetadata(WONDERSHOOTER, new FixedMetadataValue(this.plugin, ((Player) event.getEntity()).getItemInHand()));
        }
    }

    private <T extends Entity> Entity spawnWonder(Wonder<T> wonder, final Entity projectile, Player shooter) {
        T newProjectile;
        if (Projectile.class.isAssignableFrom(wonder.getEntityClass())) {
            Wonder<Projectile> w = (Wonder<Projectile>) wonder;
            newProjectile = (T) shooter.launchProjectile(w.getEntityClass());
        } else {
            Vector velocity = projectile.getVelocity();
            Location current = projectile.getLocation();
            Vector velocityUnit = velocity.normalize().multiply(3);

            // Set location ahead
            Location newLocation = current.clone();
            newLocation.setX(current.getX() + velocityUnit.getX());
            newLocation.setY(current.getY() + velocityUnit.getY());
            newLocation.setZ(current.getZ() + velocityUnit.getZ());

            newProjectile = shooter.getWorld().spawn(newLocation, wonder.getEntityClass());
            newProjectile.setVelocity(projectile.getVelocity());
        }
        return newProjectile;
    }

    @EventHandler
    void onHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata(WONDERMETA)) {
            for (MetadataValue value : event.getEntity().getMetadata(WONDERMETA)) {
                if (value.getOwningPlugin().equals(this.plugin)) {
                    Wonder wonder = (Wonder) value.value();
                    wonder.onHit(event.getEntity());
                    break;
                }
            }
        }
    }
}