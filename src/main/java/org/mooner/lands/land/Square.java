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
        for (int x = -distance; x <= distance; x = distance) {
            for (int z = -distance; z <= distance; z++) {
                action.accept(new int[]{x, z});
                action.accept(new int[]{z, x});
            }
        }
    }

    public boolean isIn(Square square) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if(square.in(min[i], min[j])) return true;
            }
        }
        return false;
    }

    public boolean in(Location loc) {
        return in(loc.getBlockX(), loc.getBlockZ());
    }

    public boolean in(int x, int z) {
        return min[0] <= x && max[0] >= x && min[1] <= z && max[1] >= z;
    }
}
