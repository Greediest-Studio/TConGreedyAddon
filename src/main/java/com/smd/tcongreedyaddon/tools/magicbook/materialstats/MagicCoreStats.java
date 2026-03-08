package com.smd.tcongreedyaddon.tools.magicbook.materialstats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import java.util.List;

public class MagicCoreStats extends AbstractMaterialStats {

    public static final String TYPE = TConGreedyTypes.MAGICCORE;

    public final float range;
    public final float critchance;

    public MagicCoreStats(float range, float critchance) {
        super(TConGreedyTypes.MAGICCORE);
        this.range = range;
        this.critchance = critchance;
    }

    @Override
    public List<String> getLocalizedInfo() {
        List<String> info = Lists.newArrayList();
        info.add(Util.translateFormatted("stat.range.value", Util.df.format(range)));
        info.add(Util.translateFormatted("stat.critchance.value", Util.df.format(critchance)));
        return info;
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate("stat.magiccore.desc"));
    }
}
