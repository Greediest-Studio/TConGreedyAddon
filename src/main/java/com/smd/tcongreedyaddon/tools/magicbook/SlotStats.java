package com.smd.tcongreedyaddon.tools.magicbook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.IMaterialStats;

import java.util.List;

public class SlotStats implements IMaterialStats {

    public static final String TYPE = TConGreedyTypes.SLOT;

    public final boolean hasLeft;
    public final boolean hasRight;

    public SlotStats(boolean hasLeft, boolean hasRight) {
        this.hasLeft = hasLeft;
        this.hasRight = hasRight;
    }

    @Override
    public String getIdentifier() {
        return TYPE;
    }

    @Override
    public String getLocalizedName() {
        return Util.translate("stat.slot.name");
    }

    @Override
    public List<String> getLocalizedInfo() {
        List<String> info = Lists.newArrayList();
        String left = hasLeft ? Util.translate("stat.slot.available") : Util.translate("stat.slot.unavailable");
        String right = hasRight ? Util.translate("stat.slot.available") : Util.translate("stat.slot.unavailable");
        info.add(Util.translateFormatted("stat.slot.left", left));
        info.add(Util.translateFormatted("stat.slot.right", right));
        return info;
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate("stat.slot.desc"));
    }
}