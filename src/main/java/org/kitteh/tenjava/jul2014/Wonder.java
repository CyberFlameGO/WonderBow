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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
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
    private enum Record {
        MOST_CONFUSED("Most Confused"),
        MOST_WITHERED("Most Withered");

        private final String loreLine;

        private Record(String loreLine) {
            this.loreLine = ChatColor.GOLD + loreLine + ": ";
        }

        private String getLoreLine() {
            return this.loreLine;
        }
    }

    private static final Random RANDOM = new Random();
    private static final List<Wonder<? extends Entity>> WONDERS = new ArrayList<>();

    private static Set<Player> getNearbyPlayers(Entity entity, int range) {
        return entity.getNearbyEntities(range, range, range).stream().filter(e -> e instanceof Player).map(e -> (Player) e).collect(Collectors.toSet());
    }

    private static ParticleTimer particles() {
        return JavaPlugin.getPlugin(WonderBow.class).getParticleTimer();
    }

    private static void record(Record record, ItemStack stack, int score) {
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        boolean set = false;
        final String loreLine = record.getLoreLine();
        for (int i = 0; i < lore.size(); i++) {
            String string = lore.get(i);
            if (string.startsWith(loreLine)) {
                int count = Integer.parseInt(string.substring((loreLine).length()));
                if (count < score) {
                    lore.set(i, loreLine + score);
                }
                set = true;
                break;
            }
        }
        if (!set) {
            lore.add(loreLine + score);
        }
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    private static final Wonder<Chicken> CHICKEN = new Wonder<>(Chicken.class, 1, chicken -> particles().addEffect(ParticleTimer.Particle.ANGRY_VILLAGER, chicken, 10, 5, bawk -> {
        for (int i = 0; i < 15; i++) {
            bawk.getWorld().strikeLightning(bawk.getLocation().clone().add(RANDOM.nextDouble() * 4 + 2, RANDOM.nextDouble() * 4 + 2, RANDOM.nextDouble() * 4 + 2));
        }
        bawk.remove();
    }), no());
    private static final Wonder<EnderPearl> ENDERS = new Wonder<>(EnderPearl.class, 1, ender -> particles().addEffect(ParticleTimer.Particle.HEART, ender, -1, 1, no()), no()); // BAD LUCK EH
    private static final Wonder<WitherSkull> SKULL = new Wonder<>(WitherSkull.class, 1, no(), skull -> {
        Set<Player> nearbyPlayers = getNearbyPlayers(skull, 3);
        if (!nearbyPlayers.isEmpty() && skull.hasMetadata("WonderShooter")) {
            record(Record.MOST_WITHERED, (ItemStack) skull.getMetadata("WonderShooter").get(0).value(), nearbyPlayers.size());
        }
        nearbyPlayers.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1, true)));
    });
    private static final Wonder<Fireball> FIRE = new Wonder<>(Fireball.class, 1, no(), fireball -> particles().addEffect(ParticleTimer.Particle.SPLASH, fireball.getWorld().spawn(fireball.getLocation(), Cow.class), 40, 1, cow -> {
        cow.getWorld().createExplosion(cow.getLocation().getX(), cow.getLocation().getY(), cow.getLocation().getZ(), 3, false, false);
        cow.getLocation().add(0, 1, 0).getBlock().setType(Material.WATER);
        cow.remove();
    }));
    private static final Wonder<Arrow> ARROW = new Wonder<>(Arrow.class, 2, arrow -> particles().addEffect(ParticleTimer.Particle.SPELL, arrow, -1, 1, no()), arrow -> {
        Set<Player> nearbyPlayers = getNearbyPlayers(arrow, 5);
        if (!nearbyPlayers.isEmpty() && arrow.hasMetadata("WonderShooter")) {
            record(Record.MOST_CONFUSED, (ItemStack) arrow.getMetadata("WonderShooter").get(0).value(), nearbyPlayers.size());
        }
        nearbyPlayers.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 4, true)));
        arrow.remove();
    });
    private static final Wonder<Egg> EGG = new Wonder<>(Egg.class, 1, egg -> particles().addEffect(ParticleTimer.Particle.RED, egg, -1, 1, no()), egg -> {
        Set<Chicken> chicks = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            Chicken chick = egg.getWorld().spawn(egg.getLocation(), Chicken.class);
            chicks.add(chick);
            chick.setVelocity(new Vector(RANDOM.nextFloat() * 0.4, RANDOM.nextFloat() * 1.5, RANDOM.nextFloat() * 0.4));
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(WonderBow.class), () -> chicks.stream().filter(Entity::isValid).forEach(chicken -> {
            chicken.remove();
            particles().broadcastEffect(ParticleTimer.Particle.LAVA.toString(), chicken.getLocation(), 0, 20);
        }), 40);
    });

    /**
     * Generates a consumer which does nothing.
     *
     * @param <T> Type to win!
     * @return consumer of nothing
     */
    static <T> Consumer<T> no() {
        return t -> {
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