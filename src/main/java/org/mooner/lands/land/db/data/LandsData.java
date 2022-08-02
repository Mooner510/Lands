package org.mooner.lands.land.db.data;

import lombok.Getter;
import org.bukkit.Material;
import org.mooner.lands.MoonerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.MoonerUtils.parseString;

@Getter
public class LandsData {
    private final String name;
    private final Material material;
    private final int size;
    private final double cost;
    private final List<String> lore;

    public LandsData(String name, Material m, int size, double cost, String... lore) {
        this.name = chat(name);
        this.material = m;
        this.size = size;
        this.cost = cost;
        List<String> l = new ArrayList<>();
        l.add("");
        l.addAll(Arrays.asList(lore));
        l.add("&7사이즈 &6" + size * 2 + "x" + size*2);
        l.add("");
        l.add("&7가격 : &b" + parseString(cost, 1, true) + "원");
        this.lore = l.stream().map(MoonerUtils::chat).toList();
    }

    public LandsData(String name, Material m, int size, double cost, List<String> lore) {
        this.name = chat(name);
        this.material = m;
        this.size = size;
        this.cost = cost;
        List<String> l = new ArrayList<>();
        l.add("");
        l.addAll(lore);
        l.add("&7사이즈 &6" + size * 2 + "x" + size*2);
        l.add("");
        l.add("&7가격 : &b" + parseString(cost, 1, true) + "원");
        this.lore = l.stream().map(MoonerUtils::chat).toList();
    }
}
