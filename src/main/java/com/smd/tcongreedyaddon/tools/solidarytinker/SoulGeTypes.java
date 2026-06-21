package com.smd.tcongreedyaddon.tools.solidarytinker;

import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;

public final class SoulGeTypes {

    public static final String SOULGE_HEART = "soulge_heart";

    private SoulGeTypes() {
    }

    public static void init() {
        Material.UNKNOWN.addStats(new SoulGeHeartStats(8.0f, 2, 6, 0.15f));
    }

    public static void registerSoulGeHeart(String materialId, float detectionRange, int exertTimes,
                                           int attackFrequency, float killThreshold) {
        Material material = TinkerRegistry.getMaterial(materialId);
        if (material != null && material.getStats(SoulGeHeartStats.TYPE) == null) {
            material.addStats(new SoulGeHeartStats(detectionRange, exertTimes, attackFrequency, killThreshold));
        }
    }

    public static PartMaterialType soulgeHeart(IToolPart part) {
        return new PartMaterialType(part, SOULGE_HEART);
    }
}
