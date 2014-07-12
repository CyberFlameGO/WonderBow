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
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Wonders are the magical tricks the WonderBow can do!
 *
 * @param <T> Entity type of this particular Wonder
 */
final class Wonder<T extends Entity> {
    private static final Random RANDOM = new Random();
    private static final List<Wonder<? extends Entity>> WONDERS = new ArrayList<>();
    private static final String MOST_WITHERED = ChatColor.GOLD + "Most Withered: ";
    private static final String MOST_CONFUSED = ChatColor.GOLD + "Most Confused: ";

    private static ParticleTimer particles() {
        return JavaPlugin.getPlugin(WonderBow.class).getParticleTimer();
    }

    private static final Wonder<Chicken> CHICKEN = new Wonder<>(Chicken.class, 1, chicken -> particles().addEffect(ParticleTimer.Particle.ANGRY_VILLAGER, chicken, 10, 5, bawk -> {
        bawk.getWorld().strikeLightning(bawk.getLocation());
        bawk.remove();
    }), no());
    private static final Wonder<EnderPearl> ENDERS = new Wonder<>(EnderPearl.class, 1, ender -> particles().addEffect(ParticleTimer.Particle.HEART, ender, -1, 1, no()), no()); // BAD LUCK EH
    private static final Wonder<WitherSkull> SKULL = new Wonder<>(WitherSkull.class, 1, no(), skull -> {
        Set<Player> nearbyPlayers = skull.getNearbyEntities(3, 3, 3).stream().filter(e -> e instanceof Player).map(e -> (Player) e).collect(Collectors.toSet());
        if (!nearbyPlayers.isEmpty() && skull.hasMetadata("WonderShooter")) {
            ItemStack shooter = (ItemStack) skull.getMetadata("WonderShooter").get(0).value();
            ItemMeta meta = shooter.getItemMeta();
            List<String> lore = meta.getLore();
            boolean set = false;
            for (int i = 0; i < lore.size(); i++) {
                String string = lore.get(i);
                if (string.startsWith(MOST_WITHERED)) {
                    int count = Integer.parseInt(string.substring((MOST_WITHERED).length()));
                    if (count < nearbyPlayers.size()) {
                        lore.set(i, MOST_WITHERED + nearbyPlayers.size());
                    }
                    set = true;
                    break;
                }
            }
            if (!set) {
                lore.add(MOST_WITHERED + nearbyPlayers.size());
            }
            meta.setLore(lore);
            shooter.setItemMeta(meta);
        }
        nearbyPlayers.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1, true)));
    });
    private static final Wonder<Fireball> FIRE = new Wonder<>(Fireball.class, 1, no(), fireball -> particles().addEffect(ParticleTimer.Particle.SPLASH, fireball.getWorld().spawn(fireball.getLocation(), Cow.class), 40, 1, cow -> {
        cow.getWorld().createExplosion(cow.getLocation().getX(), cow.getLocation().getY(), cow.getLocation().getZ(), 3, false, false);
        cow.getLocation().add(0, 1, 0).getBlock().setType(Material.WATER);
        cow.remove();
    }));
    private static final Wonder<Arrow> ARROW = new Wonder<>(Arrow.class, 1, arrow -> particles().addEffect(ParticleTimer.Particle.SPELL, arrow, -1, 1, no()), arrow -> {
        Set<Player> nearbyPlayers = arrow.getNearbyEntities(5, 5, 5).stream().filter(e -> e instanceof Player).map(e -> (Player) e).collect(Collectors.toSet());
        if (!nearbyPlayers.isEmpty() && arrow.hasMetadata("WonderShooter")) {
            ItemStack shooter = (ItemStack) arrow.getMetadata("WonderShooter").get(0).value();
            ItemMeta meta = shooter.getItemMeta();
            List<String> lore = meta.getLore();
            boolean set = false;
            for (int i = 0; i < lore.size(); i++) {
                String string = lore.get(i);
                if (string.startsWith(MOST_CONFUSED)) {
                    int count = Integer.parseInt(string.substring((MOST_CONFUSED).length()));
                    if (count < nearbyPlayers.size()) {
                        lore.set(i, MOST_CONFUSED + nearbyPlayers.size());
                    }
                    set = true;
                    break;
                }
            }
            if (!set) {
                lore.add(MOST_CONFUSED + nearbyPlayers.size());
            }
            meta.setLore(lore);
            shooter.setItemMeta(meta);
        }
        nearbyPlayers.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 4, true)));
        arrow.remove();
    });

    /**
     * Generates a consumer which does nothing.
     *
     * @param <T> Type to win!
     * @return consumer of nothing
     */
    static <T> Consumer<T> no() {
        return e -> {
        };
    }

    /**
     * Gets a random Wonder!
     *
     * @return a wonder
     */
    public static Wonder getWonder() {
        return WONDERS.get(RANDOM.nextInt(WONDERS.size()));
    }

    private final Consumer<T> processHit;
    private final Consumer<T> processSpawn;
    private final Class<T> entityClass;
    private final int weight;

    private Wonder(Class<T> entityClass, int weight) {
        this(entityClass, weight, no(), no());
    }

    private Wonder(Class<T> entityClass, int weight, Consumer<T> processSpawn, Consumer<T> processHit) {
        this.processHit = processHit;
        this.processSpawn = processSpawn;
        this.entityClass = entityClass;
        this.weight = weight;
        for (int i = 0; i < weight; i++) {
            WONDERS.add(this);
        }
    }

    /**
     * Gets the class to be spawned for this Wonder.
     *
     * @return spawning class
     */
    public Class<T> getEntityClass() {
        return this.entityClass;
    }

    /**
     * Process a wonder-spawned entity being hit.
     *
     * @param entity entity
     */
    void onHit(T entity) {
        this.processHit.accept(entity);
    }

    /**
     * Process a wonder-spawned entity being spawned.
     *
     * @param entity spawned entity
     */
    void onSpawn(T entity) {
        this.processSpawn.accept(entity);
    }

    /**
     * Gets the weight of this Wonder.
     *
     * @return weight
     */
    public int getWeight() {
        return this.weight;
    }
}