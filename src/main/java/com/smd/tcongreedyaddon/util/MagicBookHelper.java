package com.smd.tcongreedyaddon.util;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.MagicCoreStats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 魔导书材料统计辅助类，提供从工具物品堆中获取各部件统计数据的方法。
 * <p>
 * 所有方法均返回可能为 {@code null} 的值
 */
public final class MagicBookHelper {

    private static final int HEAD_INDEX = 0;      // 书壳
    private static final int HANDLE_INDEX = 1;    // 铰链
    private static final int PAGE_INDEX = 2;       // 书页
    private static final int CORE_INDEX = 3;       // 术式核心

    private MagicBookHelper() {} // 禁止实例化

    /**
     * 从工具物品堆中解析出材料列表。
     * @param stack 工具物品堆
     * @return 材料列表（可能为空列表，但不会为 null）
     */
    public static List<Material> getMaterials(ItemStack stack) {
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(stack);
        return TinkerUtil.getMaterialsFromTagList(materialsTag);
    }

    /**
     * 获取书壳（头部）的材料统计。
     * @param stack 工具物品堆
     * @return 头部统计，若材料不足或类型不匹配则返回 {@code null}
     */
    @Nullable
    public static HeadMaterialStats getHeadStats(ItemStack stack) {
        List<Material> materials = getMaterials(stack);
        if (materials.size() > HEAD_INDEX) {
            return materials.get(HEAD_INDEX).getStatsOrUnknown(HeadMaterialStats.TYPE);
        }
        return null;
    }

    /**
     * 获取铰链（手柄）的材料统计。
     * @param stack 工具物品堆
     * @return 手柄统计，若材料不足或类型不匹配则返回 {@code null}
     */
    @Nullable
    public static HandleMaterialStats getHandleStats(ItemStack stack) {
        List<Material> materials = getMaterials(stack);
        if (materials.size() > HANDLE_INDEX) {
            return materials.get(HANDLE_INDEX).getStatsOrUnknown(HandleMaterialStats.TYPE);
        }
        return null;
    }

    /**
     * 获取书页的材料统计。
     * @param stack 工具物品堆
     * @return 书页统计，若材料不足或类型不匹配则返回 {@code null}
     */
    @Nullable
    public static BookPageStats getBookPageStats(ItemStack stack) {
        List<Material> materials = getMaterials(stack);
        if (materials.size() > PAGE_INDEX) {
            return materials.get(PAGE_INDEX).getStatsOrUnknown(BookPageStats.TYPE);
        }
        return null;
    }

    /**
     * 获取书页的施法速度。
     * @param stack 工具物品堆
     * @return 施法速度值，若无法获取则返回 {@code null}
     */
    @Nullable
    public static Integer getSpellSpeed(ItemStack stack) {
        BookPageStats stats = getBookPageStats(stack);
        return stats != null ? stats.spellspeed : null;
    }

    /**
     * 获取左槽是否可用。
     * @param stack 工具物品堆
     * @return {@code true} 表示左槽可用，{@code false} 表示不可用，若无法获取则返回 {@code null}
     */
    @Nullable
    public static Boolean hasLeftSlot(ItemStack stack) {
        BookPageStats stats = getBookPageStats(stack);
        return stats != null ? stats.hasLeft : null;
    }

    /**
     * 获取右槽是否可用。
     * @param stack 工具物品堆
     * @return {@code true} 表示右槽可用，{@code false} 表示不可用，若无法获取则返回 {@code null}
     */
    @Nullable
    public static Boolean hasRightSlot(ItemStack stack) {
        BookPageStats stats = getBookPageStats(stack);
        return stats != null ? stats.hasRight : null;
    }

    /**
     * 获取术式核心的材料统计。
     * @param stack 工具物品堆
     * @return 术式核心统计，若材料不足或类型不匹配则返回 {@code null}
     */
    @Nullable
    public static MagicCoreStats getMagicCoreStats(ItemStack stack) {
        List<Material> materials = getMaterials(stack);
        if (materials.size() > CORE_INDEX) {
            return materials.get(CORE_INDEX).getStatsOrUnknown(MagicCoreStats.TYPE);
        }
        return null;
    }

    /**
     * 获取术式核心的施法范围。
     * @param stack 工具物品堆
     * @return 范围值，若无法获取则返回 {@code null}
     */
    @Nullable
    public static Float getRange(ItemStack stack) {
        MagicCoreStats stats = getMagicCoreStats(stack);
        return stats != null ? stats.range : null;
    }

    /**
     * 获取术式核心的暴击几率。
     * @param stack 工具物品堆
     * @return 暴击几率值，若无法获取则返回 {@code null}
     */
    @Nullable
    public static Float getCritChance(ItemStack stack) {
        MagicCoreStats stats = getMagicCoreStats(stack);
        return stats != null ? stats.critchance : null;
    }


    /**
     * 获取魔导书的攻击力（来自头部和手柄等，使用 Tinkers' 的计算方式）。
     * @param stack 工具物品堆
     * @return 实际攻击力，若无法计算则返回 0（此方法来自 Tinkers'，可能返回 0 表示无攻击力）
     */
    public static float getAttackDamage(ItemStack stack) {
        return ToolHelper.getActualAttack(stack);
    }

}