package org.kitteh.tenjava.jul2014;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class WonderTest {
    @Test
    public void weight() throws IllegalAccessException {
        Set<String> underweight = Arrays.stream(Wonder.class.getDeclaredFields()).filter(field -> field.getType().equals(Wonder.class)).filter(field -> {
            try {
                field.setAccessible(true);
                Wonder wonder = (Wonder) field.get(null);
                return wonder.getWeight() < 1;
            } catch (Exception ignored) {
            }
            return false;
        }).map(Field::getName).collect(Collectors.toSet());
        if (!underweight.isEmpty()) {
            throw new AssertionError("Dangerously underweight: " + underweight);
        }
    }
}