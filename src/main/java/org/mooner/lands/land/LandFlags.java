package org.mooner.lands.land;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum LandFlags {
    MOVE_IN("입장", true, LandFlagSetting.DENY, Material.OAK_DOOR),
    MOVE_OUT("퇴장", true, LandFlagSetting.DENY, Material.DARK_OAK_DOOR),
    BLOCK_PLACE("건축", true, LandFlagSetting.DENY, Material.BRICKS),
    BLOCK_BREAK("채굴 및 파괴", true, LandFlagSetting.DENY, Material.IRON_PICKAXE),
    OPEN("문 열기", true, LandFlagSetting.DENY, Material.IRON_DOOR),
    USE_BUTTON("버튼 및 레버 사용", true, LandFlagSetting.DENY, Material.STONE_BUTTON),
    USE_PLATE("갑압판 및 실 밟기", true, LandFlagSetting.DENY, Material.OAK_PRESSURE_PLATE),
    USE_REDSTONE("중계기, 비교기 사용", true, LandFlagSetting.DENY, Material.REPEATER),
    USE_CHEST("상자 사용", true, LandFlagSetting.DENY, Material.CHEST),
    USE_ANVIL("모루 사용", true, LandFlagSetting.DENY, Material.ANVIL),
    USE_ENCHANTMENTS("마법 부여대, 숫돌 사용", true, LandFlagSetting.ALLOW, Material.ENCHANTING_TABLE),
    USE_JUKEBOX("주크박스 사용", true, LandFlagSetting.DENY, Material.MUSIC_DISC_CAT),
    PLAY_NOTE_BLOCK("노트블럭 연주", true, LandFlagSetting.DENY, Material.JUKEBOX),
    EDIT_NOTE_BLOCK("노트블럭 조율", true, LandFlagSetting.DENY, Material.NOTE_BLOCK),
    ENDER_PEARL_TELEPORT("엔더진주 사용", true, LandFlagSetting.DENY, Material.ENDER_PEARL),
    USE_ITEM_FRAME("아이템 거치대 사용", true, LandFlagSetting.DENY, Material.ITEM_FRAME),
    PROTECT_ITEM_FRAME("몹으로부터 아이템 거치대 보호", false, LandFlagSetting.DENY, Material.GLOW_ITEM_FRAME),
    RIDE("탑승", true, LandFlagSetting.DENY, Material.SADDLE),
    EXPLODE("폭발", false, LandFlagSetting.DENY, Material.TNT),
    LIGHTNING("번개", false, LandFlagSetting.DENY, Material.LIGHTNING_ROD),
    ITEM_PICKUP("아이템 줍기", true, LandFlagSetting.DENY, Material.WHEAT_SEEDS),
    ITEM_THROW("아이템 던지기", true, LandFlagSetting.DENY, Material.PUMPKIN_SEEDS),
    FIRE("불 확산", false, LandFlagSetting.DENY, Material.FLINT_AND_STEEL),
    PVP("플레이어간 전투", false, LandFlagSetting.ALLOW, Material.DIAMOND_SWORD),
    ENTITY_DAMAGE_BY_PLAYER("몬스터 공격", true, LandFlagSetting.ALLOW, Material.ZOMBIE_HEAD),
//    ENTITY_TARGET("몬스터 타겟", true, LandFlagSetting.ALLOW),
    PLAYER_DAMAGE_BY_ENTITY("몬스터 피해", true, LandFlagSetting.ALLOW, Material.SKELETON_SKULL),
    ENTITY_SPAWN("몹 스폰", false, LandFlagSetting.ALLOW, Material.ZOMBIE_SPAWN_EGG),
    ENDERMAN_BLOCK("엔더맨 블록 피해", false, LandFlagSetting.DENY, Material.ENDERMAN_SPAWN_EGG),
//    FLOW("액체 흐름"),
    LAVA_FLOW("용암 흐름", false, LandFlagSetting.DENY, Material.LAVA_BUCKET),
    GROW("작물 성장", false, LandFlagSetting.ALLOW, Material.WHEAT),
    TRADE("주민 거래", true, LandFlagSetting.ALLOW, Material.EMERALD),
    FRUIT_TELEPORT("후렴과 순간이동", true, LandFlagSetting.DENY, Material.CHORUS_FRUIT),
    EXP_PICKUP("경험치 획득", true, LandFlagSetting.ALLOW, Material.EXPERIENCE_BOTTLE);

    public enum LandFlagSetting {
        DEFAULT,
        ALLOW,
        ONLY_COOP,
        DENY,
    }

    private final String tag;
    private final boolean playerFlag;
    private final LandFlagSetting defaultSetting;
    private final Material material;

    LandFlags(String s, boolean p, LandFlagSetting def, Material material) {
        this.tag = s;
        this.playerFlag = p;
        this.defaultSetting = def;
        this.material = material;
    }
}
