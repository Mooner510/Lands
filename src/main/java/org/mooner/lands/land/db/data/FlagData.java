package org.mooner.lands.land.db.data;

import org.mooner.lands.land.LandFlags;

public record FlagData(int getId, int getLand, LandFlags getFlag, LandFlags.LandFlagSetting getSetting) {
}
