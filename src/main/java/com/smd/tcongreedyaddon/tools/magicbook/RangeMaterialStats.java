package com.smd.tcongreedyaddon.tools.magicbook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.IMaterialStats;

import java.util.List;

public class RangeMaterialStats implements IMaterialStats {

    public static final String TYPE = TConGreedyTypes.RANGE;

    public final float range;

    public RangeMaterialStats(float range) {
        this.range = range;
    }

    @Override
    public String getIdentifier() {
        return TYPE;
    }

    @Override
    public String getLocalizedName() {
        return Util.translate("stat.range.name");
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