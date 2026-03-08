package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.CritChanceStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.RangeMaterialStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.SlotStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.SpellSpeedStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;

public class TConGreedyTypes {

    public static final String SLOT = Tags.MOD_ID + ".slot";
    public static final String RANGE = Tags.MOD_ID + ".range";
    public static final String CRITCHANCE = Tags.MOD_ID +".critchance";
    public static final String SPELLSPEED = Tags.MOD_ID +".spellspeed";

    public static void init() {
        Material.UNKNOWN.addStats(new SlotStats(true, true));
        Material.UNKNOWN.addStats(new RangeMaterialStats(10.0F));
        Material.UNKNOWN.addStats(new CritChanceStats(10.0F));
        Material.UNKNOWN.addStats(new SpellSpeedStats(1));
    }

    public static PartMaterialType bookpage(IToolPart part) {
        return new PartMaterialType(part, SLOT, SPELLSPEED);
    }

    public static PartMaterialType magiccore(IToolPart part) {
        return new PartMaterialType(part, RANGE, CRITCHANCE);
    }

}