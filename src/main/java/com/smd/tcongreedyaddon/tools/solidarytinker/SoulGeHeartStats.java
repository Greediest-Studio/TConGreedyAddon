package com.smd.tcongreedyaddon.tools.solidarytinker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import java.util.List;

public class SoulGeHeartStats extends AbstractMaterialStats {

    public static final String TYPE = SoulGeTypes.SOULGE_HEART;

    public final float detectionRange;
    public final int exertTimes;
    public final int attackFrequency;
    public final float killThreshold;

    public SoulGeHeartStats(float detectionRange, int exertTimes, int attackFrequency, float killThreshold) {
        super(TYPE);
        this.detectionRange = detectionRange;
        this.exertTimes = exertTimes;
        this.attackFrequency = attackFrequency;
        this.killThreshold = killThreshold;
    }

    @Override
    public List<String> getLocalizedInfo() {
        List<String> info = Lists.newArrayList();
        info.add(Util.translateFormatted("stat.soulge.detection_range.name", Util.df.format(detectionRange)));
        info.add(Util.translateFormatted("stat.soulge.exert_times.name", exertTimes));
        info.add(Util.translateFormatted("stat.soulge.attack_frequency.name", attackFrequency));
        info.add(Util.translateFormatted("stat.soulge.kill_threshold.name", Math.round(killThreshold * 100.0f)));
        return info;
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate("stat.soulge_heart.desc"));
    }
}
