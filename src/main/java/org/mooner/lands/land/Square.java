package org.mooner.lands.land;

import lombok.Getter;
import org.bukkit.Location;

import java.util.function.Consumer;

public class Square {
    @Getter
    private final int x;
    @Getter
    private final int z;
    @Getter
    private final int distance;

    private final int[] min;
    private final int[] max;

    public Square(int x, int z, int distance) {
        this.x = x;
        this.z = z;
        this.distance = distance;

        min = new int[]{x-distance, z-distance};
        max = new int[]{x+distance, z+distance};
    }

    public void getOutline(Consumer<int[]> action) {
        for (int z = -distance; z < distance; z++) {
            action.accept(new int[]{-distance, z});
            if(z != -distance)
                action.accept(new int[]{z, -distance});
        }
        for (int z = -distance; z < distance; z++) {
            action.accept(new int[]{distance, z});
            action.accept(new int[]{z, distance});
        }
    }

    public boolean isIn(Square square) {
        return in(square.min[0], square.min[1]) || in(square.min[0], square.max[1]) || in(square.max[0], square.min[1]) || in(square.max[0], square.max[1]);
    }

    public boolean in(Location loc) {
        return in(loc.getBlockX(), loc.getBlockZ());
    }

    public boolean in(int x, int z) {
        return min[0] <= x && max[0] >= x && min[1] <= z && max[1] >= z;
    }
}
