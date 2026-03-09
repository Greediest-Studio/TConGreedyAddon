package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.*;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;

public class TConGreedyTypes {

    public static final String BOOKPAGE = Tags.MOD_ID +".bookpage";
    public static final String MAGICCORE = Tags.MOD_ID +".magiccore";

    public static void init() {
        Material.UNKNOWN.addStats(new BookPageStats(1, 1, 1));
        Material.UNKNOWN.addStats(new MagicCoreStats(10.0F,10F));
    }

    public static PartMaterialType bookpage(IToolPart part) {
        return new PartMaterialType(part, BOOKPAGE);
    }

    public static PartMaterialType magiccore(IToolPart part) {
        return new PartMaterialType(part, MAGICCORE);
    }

}