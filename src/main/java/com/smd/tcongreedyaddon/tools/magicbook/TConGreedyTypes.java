package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.Tags;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;

public class TConGreedyTypes {

    public static final String SLOT = Tags.MOD_ID + ".slot";
    public static final String RANGE = Tags.MOD_ID + ".range";

    public static void init() {
        Material.UNKNOWN.addStats(new SlotStats(true, true));
        Material.UNKNOWN.addStats(new RangeMaterialStats(10.0F));
    }

    public static PartMaterialType slot(IToolPart part) {
        return new PartMaterialType(part, SLOT);
    }

    public static PartMaterialType range(IToolPart part) {
        return new PartMaterialType(part, RANGE);
    }

}