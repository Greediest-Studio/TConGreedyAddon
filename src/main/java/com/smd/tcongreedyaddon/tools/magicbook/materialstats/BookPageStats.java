package com.smd.tcongreedyaddon.tools.magicbook.materialstats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import java.util.List;

public class BookPageStats extends AbstractMaterialStats {

    public static final String TYPE = TConGreedyTypes.BOOKPAGE;

    public final int leftSlots;
    public final int rightSlots;
    public final int spellspeed;


    public BookPageStats(int leftSlots, int rightSlots, int spellspeed) {
        super(TConGreedyTypes.BOOKPAGE);
        this.leftSlots = leftSlots;
        this.rightSlots = rightSlots;
        this.spellspeed = spellspeed;
    }

    @Override
    public List<String> getLocalizedInfo() {
        List<String> info = Lists.newArrayList();
        info.add(Util.translateFormatted("stat.slot.left", leftSlots));
        info.add(Util.translateFormatted("stat.slot.right", rightSlots));
        info.add(Util.translateFormatted("stat.spellspeed.value", Util.df.format(spellspeed)));

        return info;
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate("stat.bookpage.desc"));
    }
}
