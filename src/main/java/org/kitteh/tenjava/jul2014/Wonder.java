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

import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class Wonder<T extends Entity> {
    private static final Random RANDOM = new Random();
    private static final List<Wonder<? extends Entity>> WONDERS = new ArrayList<>();

    private static final Wonder<Chicken> CHICKEN = new Wonder<>(Chicken.class);
    private static final Wonder<EnderPearl> ENDERS = new Wonder<>(EnderPearl.class, e -> e.setShooter(null), no());
    private static final Wonder<WitherSkull> SKULL = new Wonder<>(WitherSkull.class);
    private static final Wonder<Fireball> FIRE = new Wonder<>(Fireball.class, no(), f -> f.getWorld().<Cow>spawn(f.getLocation(), Cow.class));

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