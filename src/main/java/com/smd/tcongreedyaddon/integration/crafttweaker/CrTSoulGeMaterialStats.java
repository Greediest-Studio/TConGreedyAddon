package com.smd.tcongreedyaddon.integration.crafttweaker;

import com.smd.tcongreedyaddon.tools.solidarytinker.SoulGeHeartStats;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.NoSuchElementException;

@ZenRegister
@ZenClass("mods.tcongreedy.TConGreedy")
public final class CrTSoulGeMaterialStats {

    private CrTSoulGeMaterialStats() {
    }

    @ZenMethod
    public static void setSoulGeHeartStats(String materialId, float detectionRange, int exertTimes,
                                           int attackFrequency, float killThreshold) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                Material mat = TinkerRegistry.getMaterial(materialId);
                if (mat == null) {
                    throw new NoSuchElementException("Unknown material: " + materialId);
                }
                mat.addStats(new SoulGeHeartStats(detectionRange, exertTimes, attackFrequency, killThreshold));
            }

            @Override
            public String describe() {
                return String.format(
                        "Setting SoulGeHeart stats for material %s to {detectionRange=%.2f, exertTimes=%d, attackFrequency=%d, killThreshold=%.3f}",
                        materialId, detectionRange, exertTimes, attackFrequency, killThreshold);
            }
        });
    }
}
