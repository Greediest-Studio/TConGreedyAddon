package com.smd.tcongreedyaddon.tools.magicbook.materialstats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import java.util.List;

public class BookPageStats extends AbstractMaterialStats {

    public static final String TYPE = TConGreedyTypes.BOOKPAGE;

    public final boolean hasLeft;
    public final boolean hasRight;
    public final int spellspeed;

    public BookPageStats(boolean hasLeft, boolean hasRight, int spellspeed) {
        super(TConGreedyTypes.BOOKPAGE);
        this.hasLeft = hasLeft;
        this.hasRight = hasRight;
        this.spellspeed = spellspeed;
    }

    @Override
    public List<String> getLocalizedInfo() {
        List<String> info = Lists.newArrayList();
        String left = hasLeft ? Util.translate("stat.slot.available") : Util.translate("stat.slot.unavailable");
        String right = hasRight ? Util.translate("stat.slot.available") : Util.translate("stat.slot.unavailable");
        info.add(Util.translateFormatted("stat.spellspeed.value", Util.df.format(spellspeed)));
        info.add(Util.translateFormatted("stat.slot.left", left));
        info.add(Util.translateFormatted("stat.slot.right", right));

        return info;
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate("stat.bookpage.desc"));
    }
}
