package com.thaumcraftmodern.focus;

import java.util.LinkedHashMap;
import java.util.Map;

/** Stable TC4 focus-upgrade ids, icons and compound-aspect costs. */
public enum FocusUpgradeType {
    POTENCY(0, "potency", cost("telum", 1)),
    FRUGAL(1, "frugal", cost("fames", 1)),
    TREASURE(2, "treasure", cost("lucrum", 1)),
    ENLARGE(3, "enlarge", cost("iter", 1)),
    ALCHEMISTS_FIRE(4, "alchemistsfire", cost("potentia", 1, "limus", 1)),
    ALCHEMISTS_FROST(5, "alchemistsfrost", cost("gelum", 1, "vinculum", 1)),
    ARCHITECT(6, "architect", cost("fabrico", 1)),
    EXTEND(7, "extend", cost("permutatio", 1)),
    SILK_TOUCH(8, "silktouch", cost("lucrum", 1)),
    FIREBALL(9, "fireball", cost("tenebrae", 1)),
    FIRE_BEAM(10, "firebeam", cost("potentia", 1, "aer", 1)),
    SCATTERSHOT(11, "scattershot", cost("gelum", 1, "telum", 1)),
    ICE_BOULDER(12, "iceboulder", cost("gelum", 1, "vitreus", 1)),
    BAT_BOMBS(13, "batbombs", cost("potentia", 1, "vinculum", 1)),
    DEVIL_BATS(14, "devilbats", cost("tutamen", 1)),
    NIGHTSHADE(15, "nightshade", cost("victus", 1, "venenum", 1, "praecantatio", 1)),
    SEEKER(16, "seeker", cost("sensus", 1, "cognitio", 1)),
    CHAIN_LIGHTNING(17, "chainlightning", cost("tempestas", 1)),
    EARTH_SHOCK(18, "earthshock", cost("tempestas", 1)),
    VAMPIRE_BATS(19, "vampirebats", cost("fames", 1, "victus", 1)),
    DOWSING(20, "dowsing", cost("perfodio", 1));

    private final short id;
    private final String key;
    private final Map<String, Integer> aspectCost;

    FocusUpgradeType(int id, String key, Map<String, Integer> aspectCost) {
        this.id = (short) id;
        this.key = key;
        this.aspectCost = aspectCost;
    }

    public short id() { return id; }
    public String key() { return key; }
    public String icon() { return "thaumic_reborn:textures/foci/" + key + ".png"; }
    public String nameKey() { return "focus.upgrade." + key + ".name"; }
    public String textKey() { return "focus.upgrade." + key + ".text"; }
    public Map<String, Integer> aspectCost() { return aspectCost; }

    public static FocusUpgradeType byId(int id) {
        for (FocusUpgradeType type : values()) if (type.id == id) return type;
        throw new IllegalArgumentException("unknown focus upgrade: " + id);
    }

    public static FocusUpgradeType byIdOrNull(int id) {
        for (FocusUpgradeType type : values()) if (type.id == id) return type;
        return null;
    }

    private static Map<String, Integer> cost(Object... entries) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], (Integer) entries[index + 1]);
        }
        return Map.copyOf(result);
    }
}
