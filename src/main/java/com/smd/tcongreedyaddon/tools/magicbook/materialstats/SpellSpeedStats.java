package com.smd.tcongreedyaddon.tools.magicbook.materialstats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import java.util.List;

public class SpellSpeedStats extends AbstractMaterialStats {

    public static final String TYPE = TConGreedyTypes.SPELLSPEED;

    public final int spellspeed;

    public SpellSpeedStats(int spellspeed) {
        super(TConGreedyTypes.SPELLSPEED);
        this.spellspeed = spellspeed;
    }

    @Override
    public List<String> getLocalizedInfo() {
        List<String> info = Lists.newArrayList();
        info.add(Util.translateFormatted("stat.spellspeed.value", Util.df.format(spellspeed)));
        return info;
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate("stat.spellspeed.desc"));
    }
}
