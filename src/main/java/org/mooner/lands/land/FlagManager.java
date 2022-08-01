package org.mooner.lands.land;

import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.data.FlagData;

import java.util.HashMap;
import java.util.List;

public class FlagManager {
    private final int landId;
    private final HashMap<LandFlags, Integer> flagMap;
    private final HashMap<Integer, LandFlags.LandFlagSetting> valueMap;
    private HashMap<LandFlags, LandFlags.LandFlagSetting> requests;

    public FlagManager(int landId, List<FlagData> data) {
        this.landId = landId;
        flagMap = new HashMap<>();
        valueMap = new HashMap<>();
        data.forEach(land -> {
            flagMap.put(land.getFlag(), land.getId());
            valueMap.put(land.getId(), land.getSetting());
        });
    }

    public boolean setFlagRequest(LandFlags flag, LandFlags.LandFlagSetting setting) {
        if(!flag.isPlayerFlag() && setting == LandFlags.LandFlagSetting.ONLY_COOP) return false;
        requests.put(flag, setting);
        return true;
    }

    public void queue() {
        if(requests.isEmpty()) return;
        requests.forEach(this::setFlag);
        requests = new HashMap<>();
    }

    public void setFlag(LandFlags flag, LandFlags.LandFlagSetting setting) {
        if(!flag.isPlayerFlag() && setting == LandFlags.LandFlagSetting.ONLY_COOP) return;
        FlagData data;
        if((data = DatabaseManager.init.setFlag(landId, flag, setting)) == null) return;
        flagMap.put(flag, data.getId());
        valueMap.put(data.getId(), data.getSetting());
    }

    public LandFlags.LandFlagSetting getFlag(LandFlags flag) {
        Integer id = flagMap.get(flag);
        if(id == null) return flag.getDefaultSetting();
        LandFlags.LandFlagSetting v;
        return (v = valueMap.get(id)) == null || v == LandFlags.LandFlagSetting.DEFAULT ? flag.getDefaultSetting() : v;
    }
}
