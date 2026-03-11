package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.*;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;

public class TConGreedyTypes {

    public static final String BOOKPAGE = Tags.MOD_ID + ".bookpage";
    public static final String MAGICCORE = Tags.MOD_ID + ".magiccore";

    /**
     * 初始化未知材料的默认属性（仅需调用一次）
     */
    public static void init() {
        Material.UNKNOWN.addStats(new BookPageStats(1, 1, 1));
        Material.UNKNOWN.addStats(new MagicCoreStats(10.0F, 10F));
        registerBookPage("iron",10,7,5);
        registerMagicCore("iron",10,10);
    }

    /**
     * 为指定材料ID注册 MagicCore 属性（若材料存在且该类型属性缺失）
     * @param materialId 材料标识符（如 "iron"）
     * @param range      施法范围
     * @param critchance 法术暴击率
     */
    public static void registerMagicCore(String materialId, float range, float critchance) {
        Material material = TinkerRegistry.getMaterial(materialId);
        if (material != null && material.getStats(MagicCoreStats.TYPE) == null) {
            material.addStats(new MagicCoreStats(range, critchance));
        }
    }

    /**
     * 为指定材料ID注册 BookPage 属性（若材料存在且该类型属性缺失）
     * @param materialId 材料标识符
     * @param values     三个整数值，对应 BookPageStats 构造参数
     */
    public static void registerBookPage(String materialId, int... values) {
        if (values.length < 3) return;
        Material material = TinkerRegistry.getMaterial(materialId);
        if (material != null && material.getStats(BookPageStats.TYPE) == null) {
            material.addStats(new BookPageStats(values[0], values[1], values[2]));
        }
    }

    public static PartMaterialType bookpage(IToolPart part) {
        return new PartMaterialType(part, BOOKPAGE);
    }

    public static PartMaterialType magiccore(IToolPart part) {
        return new PartMaterialType(part, MAGICCORE);
    }
}