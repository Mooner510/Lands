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
        requests = new HashMap<>();
        if(data != null) data.forEach(land -> {
            flagMap.put(land.getFlag(), land.getId());
            valueMap.put(land.getId(), land.getSetting());
        });
    }

    public boolean setFlagRequest(LandFlags flag, LandFlags.LandFlagSetting setting) {
        if(!flag.isPlayerFlag() && setting == LandFlags.LandFlagSetting.ONLY_COOP) return false;
        requests.put(flag, setting);
        return true;
    }

    public LandFlags.LandFlagSetting nextFlagRequest(LandFlags flag) {
        LandFlags.LandFlagSetting f = getRealFlag(flag);
        if( LandFlags.LandFlagSetting.values().length - 1 == f.ordinal()) {
            f = LandFlags.LandFlagSetting.values()[0];
        } else {
            f = LandFlags.LandFlagSetting.values()[f.ordinal()];
        }
        if(!flag.isPlayerFlag() && f == LandFlags.LandFlagSetting.ONLY_COOP) {
            f = LandFlags.LandFlagSetting.values()[f.ordinal()];
        }
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
        if((data = DatabaseManager.init.setFlag(landId, flag, setting)) == null) return;
        if(setting == LandFlags.LandFlagSetting.DEFAULT) {
            flagMap.remove(flag);
            valueMap.remove(data.getId());
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
        LandFlags.LandFlagSetting v = requests.get(flag);
        if(v != null) return v == LandFlags.LandFlagSetting.DEFAULT ? flag.getDefaultSetting() : v;
        Integer id = flagMap.get(flag);
        if(id == null) return flag.getDefaultSetting();
        return (v = valueMap.get(id)) == null || v == LandFlags.LandFlagSetting.DEFAULT ? flag.getDefaultSetting() : v;
    }
}
