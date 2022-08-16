package org.mooner.lands.land;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum LandFlags {
    MOVE_IN("입장", true, true, LandFlagSetting.DENY, Material.OAK_DOOR),
    MOVE_OUT("퇴장", true, true, LandFlagSetting.DENY, Material.DARK_OAK_DOOR),
    BLOCK_PLACE("건축", true, true, LandFlagSetting.DENY, Material.BRICKS),
    BLOCK_BREAK("채굴 및 파괴", true, true, LandFlagSetting.DENY, Material.IRON_PICKAXE),
    OPEN("문 열기", true, true, LandFlagSetting.DENY, Material.IRON_DOOR),
    USE_BUTTON("버튼 및 레버 사용", true, true, LandFlagSetting.DENY, Material.STONE_BUTTON),
    USE_PLATE("갑압판 및 실 밟기", true, true, LandFlagSetting.DENY, Material.OAK_PRESSURE_PLATE),
    USE_REDSTONE("중계기, 비교기 사용", true, true, LandFlagSetting.DENY, Material.REPEATER),
    USE_CHEST("상자, 셜커 상자, 호퍼 사용", true, true, LandFlagSetting.DENY, Material.CHEST),
    USE_BED("침대 사용", true, true, LandFlagSetting.DENY, Material.RED_BED),
    USE_FURNACE("화로 사용", true, true, LandFlagSetting.DENY, Material.FURNACE),
    USE_ANVIL("모루 사용", true, true, LandFlagSetting.DENY, Material.ANVIL),
    USE_FARM_BLOCK("화분, 퇴비통 사용", true, true, LandFlagSetting.DENY, Material.COMPOSTER),
    USE_BEE("양봉", true, true, LandFlagSetting.DENY, Material.BEE_NEST),
    USE_LODESTONE("자석석 사용", true, true, LandFlagSetting.DENY, Material.LODESTONE),
    USE_ENCHANTMENTS("마법 부여대, 숫돌 사용", true, true, LandFlagSetting.ALLOW, Material.ENCHANTING_TABLE),
    USE_JUKEBOX("주크박스 사용", true, true, LandFlagSetting.DENY, Material.MUSIC_DISC_CAT),
    USE_PISTON("외부 피스톤 영향", LandFlagSetting.DENY, Material.PISTON),
    CREATE_SHOP("상점 생성", true, true, LandFlagSetting.DENY, Material.GOLD_INGOT),
    PLAY_NOTE_BLOCK("노트블럭 연주", true, false, LandFlagSetting.ALLOW, Material.JUKEBOX),
    EDIT_NOTE_BLOCK("노트블럭 조율", true, false, LandFlagSetting.DENY, Material.NOTE_BLOCK),
    ENDER_PEARL_TELEPORT("엔더진주 사용", true, true, LandFlagSetting.DENY, Material.ENDER_PEARL),
    USE_ITEM_FRAME("아이템 거치대 사용", true, true, LandFlagSetting.DENY, Material.ITEM_FRAME),
    PROTECT_ITEM_FRAME("몹으로부터 아이템 거치대 보호", LandFlagSetting.DENY, Material.GLOW_ITEM_FRAME),
    SNOW("눈 쌓임", LandFlagSetting.DENY, Material.SNOW),
    RIDE("탑승", true, true, LandFlagSetting.DENY, Material.SADDLE),
    LIGHTNING("번개", LandFlagSetting.DENY, Material.LIGHTNING_ROD),
    ITEM_PICKUP("아이템 줍기", true, true, LandFlagSetting.DENY, Material.WHEAT_SEEDS),
    ITEM_THROW("아이템 던지기", true, true, LandFlagSetting.DENY, Material.PUMPKIN_SEEDS),
    FIRE("불 확산", LandFlagSetting.DENY, Material.FLINT_AND_STEEL),
    PVP("플레이어간 전투", LandFlagSetting.ALLOW, Material.DIAMOND_SWORD),
    ENTITY_DAMAGE_BY_PLAYER("몬스터 공격", true, false, LandFlagSetting.ALLOW, Material.ZOMBIE_HEAD),
//    ENTITY_TARGET("몬스터 타겟", true, LandFlagSetting.ALLOW),
    PLAYER_DAMAGE_BY_ENTITY("몬스터 피해", true, false, LandFlagSetting.ALLOW, Material.SKELETON_SKULL),
    ENTITY_SPAWN("몹 스폰", LandFlagSetting.ALLOW, Material.ZOMBIE_SPAWN_EGG),
    ENDERMAN_BLOCK("엔더맨 블록 피해", LandFlagSetting.DENY, Material.ENDERMAN_SPAWN_EGG),
//    FLOW("액체 흐름"),
    LAVA_FLOW("용암 흐름", LandFlagSetting.DENY, Material.LAVA_BUCKET),
    GROW("작물 성장", LandFlagSetting.ALLOW, Material.WHEAT),
    TRADE("주민 거래", true, true, LandFlagSetting.ALLOW, Material.EMERALD),
    FRUIT_TELEPORT("후렴과 순간이동", true, false, LandFlagSetting.DENY, Material.CHORUS_FRUIT),
    EXP_PICKUP("경험치 획득", true, false, LandFlagSetting.ALLOW, Material.EXPERIENCE_BOTTLE);

    @Getter
    public enum LandFlagSetting {
        DEFAULT("&7", "기본값"),
        ALLOW("&a", "허용"),
        ONLY_COOP("&b", "공유 플레이어만"),
        DENY("&c", "거부");

        private final String color;
        private final String tag;

        LandFlagSetting(String c, String tag) {
            this.color = c;
            this.tag = tag;
        }
    }

    private final String tag;
    private final boolean playerFlag;
    private final boolean forcedOwner;
    private final LandFlagSetting defaultSetting;
    private final Material material;

    LandFlags(String s, boolean p, boolean o, LandFlagSetting def, Material material) {
        this.tag = s;
        this.playerFlag = p;
        this.forcedOwner = o;
        this.defaultSetting = def;
        this.material = material;
    }

    LandFlags(String s, LandFlagSetting def, Material material) {
        this.tag = s;
        this.playerFlag = false;
        this.forcedOwner = false;
        this.defaultSetting = def;
        this.material = material;
    }
}
