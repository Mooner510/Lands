package org.mooner.lands.land;

import lombok.Getter;

@Getter
public enum LandFlags {
    MOVE_IN("입장", true, LandFlagSetting.DENY),
    MOVE_OUT("퇴장", true, LandFlagSetting.DENY),
    BLOCK_PLACE("건축", true, LandFlagSetting.DENY),
    BLOCK_BREAK("채굴 및 파괴", true, LandFlagSetting.DENY),
    OPEN("문 열기", true, LandFlagSetting.DENY),
    USE_BUTTON("버튼 및 레버 사용", true, LandFlagSetting.DENY),
    USE_PLATE("갑압판 및 실 밟기", true, LandFlagSetting.DENY),
    USE_CHEST("상자 사용", true, LandFlagSetting.DENY),
    USE_ANVIL("모루 사용", true, LandFlagSetting.DENY),
    USE_ENCHANTMENTS("마법 부여대, 숫돌 사용", true, LandFlagSetting.ALLOW),
    ENDER_PEARL_TELEPORT("엔더진주 사용", true, LandFlagSetting.DENY),
    USE_ITEM_FRAME("아이템 거치대 사용", true, LandFlagSetting.DENY),
    PROTECT_ITEM_FRAME("몹으로부터 아이템 거치대 보호", false, LandFlagSetting.DENY),
    RIDE("탑승", true, LandFlagSetting.DENY),
    EXPLODE("폭발", false, LandFlagSetting.DENY),
    LIGHTNING("번개", false, LandFlagSetting.DENY),
    ITEM_PICKUP("아이템 줍기", true, LandFlagSetting.DENY),
    ITEM_THROW("아이템 던지기", true, LandFlagSetting.DENY),
    FIRE("불 확산", false, LandFlagSetting.DENY),
    PVP("플레이어간 전투", false, LandFlagSetting.ALLOW),
    ENTITY_DAMAGE_BY_PLAYER("몬스터 공격", true, LandFlagSetting.ALLOW),
//    ENTITY_TARGET("몬스터 타겟", true, LandFlagSetting.ALLOW),
    PLAYER_DAMAGE_BY_ENTITY("몬스터 피해", true, LandFlagSetting.ALLOW),
    ENTITY_SPAWN("몹 스폰", false, LandFlagSetting.ALLOW),
    ENDERMAN_BLOCK("엔더맨 블록 피해", false, LandFlagSetting.DENY),
//    FLOW("액체 흐름"),
    LAVA_FLOW("용암 흐름", false, LandFlagSetting.DENY),
    GROW("작물 성장", false, LandFlagSetting.ALLOW),
    FRUIT_TELEPORT("후렴과 순간이동", true, LandFlagSetting.DENY),
    EXP_PICKUP("경험치 줍기", true, LandFlagSetting.ALLOW);

    public enum LandFlagSetting {
        ALLOW,
        ONLY_COOP,
        DEFAULT,
        DENY,
    }

    private final String tag;
    private final boolean playerFlag;
    private final LandFlagSetting defaultSetting;

    LandFlags(String s, boolean p, LandFlagSetting def) {
        this.tag = s;
        this.playerFlag = p;
        this.defaultSetting = def;
    }
}
