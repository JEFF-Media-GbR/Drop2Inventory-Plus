package de.jeff_media.drop2inventory.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SimpleLocation {

    private Vector vector;
    private World world;

    public SimpleLocation(@NotNull Location location) {
        vector = location.toVector();
        world = location.getWorld();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLocation that = (SimpleLocation) o;
        return vector.equals(that.vector) && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vector, world);
    }

    @Override
    public String toString() {
        return "SimpleLocation{" +
                "vector=" + vector +
                ", world=" + world +
                '}';
    }
}
