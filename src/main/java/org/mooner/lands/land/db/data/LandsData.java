package org.mooner.lands.land.db.data;

import lombok.Getter;
import org.mooner.lands.MoonerUtils;

import java.util.List;

import static org.mooner.lands.MoonerUtils.chat;

@Getter
public class LandsData {
    private final String name;
    private final int size;
    private final double cost;
    private final List<String> lore;

    public LandsData(String name, int size, double cost, String... lore) {
        this.name = chat(name);
        this.size = size;
        this.cost = cost;
        this.lore = List.of(lore).stream().map(MoonerUtils::chat).toList();
    }

    public LandsData(String name, int size, double cost, List<String> lore) {
        this.name = chat(name);
        this.size = size;
        this.cost = cost;
        this.lore = lore.stream().map(MoonerUtils::chat).toList();
    }
}
