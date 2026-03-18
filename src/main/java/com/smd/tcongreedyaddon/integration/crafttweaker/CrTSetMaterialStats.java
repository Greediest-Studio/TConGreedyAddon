package com.smd.tcongreedyaddon.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.MagicCoreStats;

import java.util.NoSuchElementException;

@ZenRegister
@ZenClass("mods.tcongreedy.TConGreedy")
public class CrTSetMaterialStats {

    @ZenMethod
    public static void setBookPageStats(String materialId, int value1, int value2, int value3) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                Material mat = TinkerRegistry.getMaterial(materialId);
                if (mat == null) {
                    throw new NoSuchElementException("Unknown material: " + materialId);
                }
                mat.addStats(new BookPageStats(value1, value2, value3));
            }

            @Override
            public String describe() {
                return String.format("Setting BookPage stats for material %s to [%d, %d, %d]",
                        materialId, value1, value2, value3);
            }
        });
    }

    @ZenMethod
    public static void setMagicCoreStats(String materialId, float range, float critChance) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                Material mat = TinkerRegistry.getMaterial(materialId);
                if (mat == null) {
                    throw new NoSuchElementException("Unknown material: " + materialId);
                }
                mat.addStats(new MagicCoreStats(range, critChance));
            }

            @Override
            public String describe() {
                return String.format("Setting MagicCore stats for material %s to {range=%.2f, critChance=%.2f}",
                        materialId, range, critChance);
            }
        });
    }
}