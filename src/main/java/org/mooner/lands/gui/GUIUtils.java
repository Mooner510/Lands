package org.mooner.lands.gui;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.mooner.lands.MoonerUtils.chat;

public class GUIUtils {
    public static ItemStack createHead(OfflinePlayer p, int amount, String name, String... lore) {
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
        skullMeta.setOwningPlayer(p);
        i.setItemMeta(skullMeta);

        ItemMeta im = i.getItemMeta();
        im.setDisplayName(chat(name));
        im.setLore(Arrays.stream(lore)
                .map(s -> chat("&7" + s))
                .collect(Collectors.toList()));
        i.setItemMeta(im);

        return i;
    }

    public static ItemStack createItem(Material m, int amount, String name, String... lore) {
        ItemStack i = new ItemStack(m, amount);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(chat(name));
        im.setLore(Arrays.stream(lore)
                .map(s -> chat("&7" + s))
                .collect(Collectors.toList()));
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack createItem(Material m, int amount, String name, List<String> lore) {
        ItemStack i = new ItemStack(m, amount);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(chat(name));
        im.setLore(lore.stream()
                .map(s -> chat("&7" + s))
                .collect(Collectors.toList()));
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack setName(ItemStack i2, String name) {
        ItemMeta meta = i2.getItemMeta();
        meta.setDisplayName(chat(name));
        i2.setItemMeta(meta);
        return i2;
    }

    public static ItemStack setInfo(ItemStack i2, String name, String... lore) {
        return setInfo(i2, name, Arrays.asList(lore));
    }

    public static ItemStack setInfo(ItemStack i2, String name, Collection<String> lore) {
        ItemMeta meta = i2.getItemMeta();
        meta.setDisplayName(chat(name));
        ArrayList<String> l = new ArrayList<>();
        for (String s : lore) l.add(chat(s));
        meta.setLore(l);
        i2.setItemMeta(meta);
        return i2;
    }

    public static ItemStack setLore(ItemStack i, Collection<String> lore) {
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> l = new ArrayList<>();
        for (String s : lore) l.add(chat(s));
        meta.setLore(l);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack addLoreFirst(ItemStack i, String... lore) {
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> l = new ArrayList<>();
        for (String s : lore) l.add(chat(s));
        if(meta.getLore() != null && meta.getLore().size() > 0) l.addAll(meta.getLore());
        meta.setLore(l);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack addLore(ItemStack i, String... lore) {
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> l = new ArrayList<>();

        if(meta.getLore() != null && meta.getLore().size() > 0) l = new ArrayList<>(meta.getLore());

        for (String s : lore) l.add(chat(s));
        meta.setLore(l);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack addLore(ItemStack i, Collection<String> lore) {
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> l = new ArrayList<>();

        if(meta.getLore() != null && meta.getLore().size() > 0) l = new ArrayList<>(meta.getLore());

        for (String s : lore) l.add(chat(s));
        meta.setLore(l);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack addLore(ItemStack i, Collection<String> lore, String... add) {
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> l = new ArrayList<>();

        if(meta.getLore() != null && meta.getLore().size() > 0) l = new ArrayList<>(meta.getLore());

        for (String s : lore) l.add(chat(s));
        for (String s : add) l.add(chat(s));
        meta.setLore(l);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack allFlags(ItemStack i) {
        ItemMeta meta = i.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack modifyFlags(ItemStack i, ItemFlag... metas) {
        ItemMeta meta = i.getItemMeta();
        meta.addItemFlags(metas);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack ench(ItemStack i) {
        ItemMeta meta = i.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(meta);
        i.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        return i;
    }

    public static ItemStack unEnch(ItemStack i) {
        i.removeEnchantment(Enchantment.DURABILITY);
        return i;
    }

    public static ItemStack colored(ItemStack i, int r, int g, int b) {
        if(i.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
            meta.setColor(Color.fromRGB(r, g, b));
            i.setItemMeta(meta);
        }
        return i;
    }

    public static ItemStack colored(ItemStack i, String hex) {
        if(i.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
            meta.setColor(Color.fromRGB(Integer.parseInt(hex,16)));
            i.setItemMeta(meta);
        }
        return i;
    }

    public static ItemStack colored(ItemStack i, int hex) {
        if(i.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
            meta.setColor(Color.fromRGB(hex));
            i.setItemMeta(meta);
        }
        return i;
    }

    public static ItemStack customHead(String nbt, int amount, String name, String... lore) {
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, amount);
        ItemMeta meta = i.getItemMeta();
        if(lore.length > 0) {
            ArrayList<String> l = new ArrayList<>();
            for (String s : lore) {
                l.add(chat(s));
            }
            meta.setLore(l);
        }
        meta.setDisplayName(chat(name));
        i.setItemMeta(meta);

        NBTItem n = new NBTItem(i);
        n.mergeCompound(new NBTContainer(nbt));
        return n.getItem();
    }
}
