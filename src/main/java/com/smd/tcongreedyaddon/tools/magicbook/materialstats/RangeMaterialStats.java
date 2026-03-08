package com.smd.tcongreedyaddon.tools.magicbook.materialstats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import java.util.List;

public class RangeMaterialStats extends AbstractMaterialStats {

    public static final String TYPE = TConGreedyTypes.RANGE;

    public final float range;

    public RangeMaterialStats(float range) {
        super(TConGreedyTypes.RANGE);
        this.range = range;
    }

    @Override
    public List<String> getLocalizedInfo() {
        List<String> info = Lists.newArrayList();
        info.add(Util.translateFormatted("stat.range.value", Util.df.format(range)));
        return info;
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate("stat.range.desc"));
    }
}