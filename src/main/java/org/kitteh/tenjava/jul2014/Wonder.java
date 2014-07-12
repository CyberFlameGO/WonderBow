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
import org.kitteh.tenjava.jul2014.effects.ParticleTimer;

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
class Wonder<T extends Entity> {
    private static final Random RANDOM = new Random();
    private static final List<Wonder<? extends Entity>> WONDERS = new ArrayList<>();

    private static ParticleTimer particles() {
        return JavaPlugin.getPlugin(WonderBow.class).getParticleTimer();
    }

    private static final Wonder<Chicken> CHICKEN = new Wonder<>(Chicken.class, chicken -> particles().addEffect(ParticleTimer.Particle.ANGRY_VILLAGER, chicken, 10, 5, bawk -> {
        bawk.getWorld().strikeLightning(bawk.getLocation());
        bawk.remove();
    }), no());
    private static final Wonder<EnderPearl> ENDERS = new Wonder<>(EnderPearl.class);
    private static final Wonder<WitherSkull> SKULL = new Wonder<>(WitherSkull.class, no(), skull -> {
        Set<LivingEntity> nearbyPlayers = skull.getNearbyEntities(3, 3, 3).stream().filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).collect(Collectors.toSet());
        if (!nearbyPlayers.isEmpty() && skull.hasMetadata("WonderShooter")) {
            ItemStack shooter = (ItemStack) skull.getMetadata("WonderShooter").get(0).value();
            ItemMeta meta = shooter.getItemMeta();
            List<String> lore = meta.getLore();
            boolean set = false;
            for (int i = 0; i < lore.size(); i++) {
                String string = lore.get(i);
                if (string.startsWith(ChatColor.GOLD + "Most Withered: ")) {
                    int count = Integer.parseInt(string.substring((ChatColor.GOLD + "Most Withered: ").length()));
                    if (count < nearbyPlayers.size()) {
                        lore.set(i, ChatColor.GOLD + "Most Withered: " + nearbyPlayers.size());
                    }
                    set = true;
                    break;
                }
            }
            if (!set) {
                lore.add(ChatColor.GOLD + "Most Withered: " + nearbyPlayers.size());
            }
            meta.setLore(lore);
            shooter.setItemMeta(meta);
        }
        nearbyPlayers.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1, true)));
    });
    private static final Wonder<Fireball> FIRE = new Wonder<>(Fireball.class, no(), fireball -> {
        particles().addEffect(ParticleTimer.Particle.SPLASH, fireball.getWorld().spawn(fireball.getLocation(), Cow.class), 40, 1, cow -> {
            cow.getWorld().createExplosion(cow.getLocation().getX(), cow.getLocation().getY(), cow.getLocation().getZ(), 3, false, false);
            cow.getLocation().add(0, 1, 0).getBlock().setType(Material.WATER);
            cow.remove();
        });
    });
    private static final Wonder<Arrow> ARROW = new Wonder<>(Arrow.class); // Boring!

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

    private Wonder(Class<T> entityClass) {
        this(entityClass, no(), no());
    }

    private Wonder(Class<T> entityClass, Consumer<T> processSpawn, Consumer<T> processHit) {
        this.processHit = processHit;
        this.processSpawn = processSpawn;
        this.entityClass = entityClass;
        WONDERS.add(this);
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
}