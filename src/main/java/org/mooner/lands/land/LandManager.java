package org.mooner.lands.land;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.mooner.lands.Lands;
import org.mooner.lands.exception.PlayerLandNotFoundException;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.data.FlagData;
import org.mooner.lands.land.listener.LandListener;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LandManager {
    private final int landId;
    private final UUID world;
    private Square square;
    private HashMap<LandFlags, Integer> flagMap;
    private HashMap<Integer, LandFlags.LandFlagSetting> valueMap;
    private HashMap<LandFlags, LandFlags.LandFlagSetting> requests;
    private LandListener listener;

    public LandManager(PlayerLand land, List<FlagData> data) throws PlayerLandNotFoundException {
        this.landId = land.getId();
        this.world = land.getSpawnLocation().getWorld().getUID();
        flagMap = new HashMap<>();
        valueMap = new HashMap<>();
        requests = new HashMap<>();
        square = land.getSquare();
        if(data != null) data.forEach(l -> {
            flagMap.put(l.getFlag(), l.getId());
            valueMap.put(l.getId(), l.getSetting());
        });
        register();
    }

    public void update(Location loc) throws PlayerLandNotFoundException {
        if(listener != null) unregisterEvent();
        square = new Square(loc.getBlockX(), loc.getBlockZ(), square.getDistance());
        Bukkit.getPluginManager().registerEvents(listener = new LandListener(landId, world, square), Lands.lands);
    }

    public void register() throws PlayerLandNotFoundException {
        if(listener != null) unregisterEvent();
        Bukkit.getPluginManager().registerEvents(listener = new LandListener(landId, world, square), Lands.lands);
        DatabaseManager.init.landManagerMap.put(landId, this);
    }

    public void unregisterEvent() {
        HandlerList.unregisterAll(listener);
        listener = null;
    }

    public void unregister() {
        unregisterEvent();
        square = null;
        flagMap = null;
        valueMap = null;
        requests = null;
        DatabaseManager.init.landManagerMap.remove(landId);
    }

    public boolean setFlagRequest(LandFlags flag, LandFlags.LandFlagSetting setting) {
        if(!flag.isPlayerFlag() && setting == LandFlags.LandFlagSetting.ONLY_COOP) return false;
        requests.put(flag, setting);
        return true;
    }

    public LandFlags.LandFlagSetting nextFlagRequest(LandFlags flag) {
        LandFlags.LandFlagSetting f = getRealFlag(flag);
        if (LandFlags.LandFlagSetting.values().length - 1 == f.ordinal()) {
            f = LandFlags.LandFlagSetting.values()[0];
        } else {
            f = LandFlags.LandFlagSetting.values()[f.ordinal()+1];
        }
        if(!flag.isPlayerFlag() && f == LandFlags.LandFlagSetting.ONLY_COOP)
            f = LandFlags.LandFlagSetting.values()[f.ordinal()+1];
        requests.put(flag, f);
        return f;
    }

    public void queue() {
        if(requests.isEmpty()) return;
        requests.forEach(this::setFlag);
        requests = new HashMap<>();
    }

    public void setFlag(LandFlags flag, LandFlags.LandFlagSetting setting) {
        if(!flag.isPlayerFlag() && setting == LandFlags.LandFlagSetting.ONLY_COOP) return;
        FlagData data;
        if((data = DatabaseManager.init.setFlag(landId, flag, setting)) == null) {
            valueMap.remove(flagMap.remove(flag));
            return;
        }
        flagMap.put(flag, data.getId());
        valueMap.put(data.getId(), data.getSetting());
    }

    public LandFlags.LandFlagSetting getRealFlag(LandFlags flag) {
        LandFlags.LandFlagSetting v = requests.get(flag);
        if(v != null) return v;
        Integer id = flagMap.get(flag);
        if(id == null) return LandFlags.LandFlagSetting.DEFAULT;
        return (v = valueMap.get(id)) == null ? LandFlags.LandFlagSetting.DEFAULT : v;
    }

    public LandFlags.LandFlagSetting getFlag(LandFlags flag) {
        LandFlags.LandFlagSetting v;// = requests.get(flag);
//        if(v != null) return v == LandFlags.LandFlagSetting.DEFAULT ? flag.getDefaultSetting() : v;
        Integer id = flagMap.get(flag);
        if(id == null) return flag.getDefaultSetting();
        return (v = valueMap.get(id)) == null || v == LandFlags.LandFlagSetting.DEFAULT ? flag.getDefaultSetting() : v;
    }
}
