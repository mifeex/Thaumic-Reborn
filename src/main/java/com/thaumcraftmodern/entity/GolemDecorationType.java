package com.thaumcraftmodern.entity;

import java.util.Arrays;
import org.jetbrains.annotations.Nullable;

/** The eight TC4 golem decorations, including their mutually-exclusive mounting points. */
public enum GolemDecorationType {
    TOP_HAT("golem_decoration_top_hat", 'H', Mount.HEADWEAR),
    GLASSES("golem_decoration_glasses", 'G', Mount.EYES),
    BOW_TIE("golem_decoration_bow_tie", 'B', Mount.BODY),
    FEZ("golem_decoration_fez", 'F', Mount.HEADWEAR),
    DART_LAUNCHER("golem_decoration_dart_launcher", 'R', Mount.LEFT_ARM),
    VISOR("golem_decoration_visor", 'V', Mount.EYES),
    ARMOR("golem_decoration_armor", 'P', Mount.BODY),
    HAMMER("golem_decoration_hammer", 'M', Mount.RIGHT_ARM);

    private final String itemId;
    private final char legacyCode;
    private final Mount mount;

    GolemDecorationType(String itemId, char legacyCode, Mount mount) {
        this.itemId = itemId;
        this.legacyCode = legacyCode;
        this.mount = mount;
    }

    public String itemId() { return itemId; }
    public char legacyCode() { return legacyCode; }
    public Mount mount() { return mount; }

    @Nullable
    public static GolemDecorationType byItemId(String itemId) {
        return Arrays.stream(values()).filter(type -> type.itemId.equals(itemId)).findFirst().orElse(null);
    }

    public enum Mount { HEADWEAR, EYES, BODY, LEFT_ARM, RIGHT_ARM }
}
