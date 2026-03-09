package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import com.smd.tcongreedyaddon.tools.magicbook.gui.BookInventory;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.MagicCoreStats;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MagicBook extends TinkerToolCore {

    public static final float BEAM_RANGE = 10.0F;
    public static final int DURABILITY_COST = 1;

    private final Map<Integer, WeakReference<BookInventory>> inventoryCache = new WeakHashMap<>();

    // NBT keys
    public static final String TAG_CUR_LEFT_INDEX = "currentLeftSpellIndex";
    public static final String TAG_CUR_RIGHT_INDEX = "currentRightSpellIndex";
    public static final String TAG_COOLDOWNS = "cooldowns";

    public MagicBook() {
        super(
                PartMaterialType.head(SpecialWeapons.cover),
                PartMaterialType.handle(SpecialWeapons.hinge),
                TConGreedyTypes.bookpage(SpecialWeapons.bookpage),
                TConGreedyTypes.magiccore(SpecialWeapons.magiccore)
        );
        addCategory(Category.WEAPON);
        setTranslationKey("magicbook").setRegistryName("magicbook");
    }

    public BookInventory getInventory(ItemStack stack) {
        int key = stack.hashCode();
        WeakReference<BookInventory> ref = inventoryCache.get(key);
        BookInventory inv = (ref != null) ? ref.get() : null;

        if (inv != null && inv.getBookStack() == stack) {
            return inv;
        }

        BookPageStats stats = getCoreBookPageStats(stack);
        int left = (stats != null) ? stats.leftSlots : 1;
        int right = (stats != null) ? stats.rightSlots : 1;
        inv = new BookInventory(stack, left, right);
        inventoryCache.put(key, new WeakReference<>(inv));
        return inv;
    }


    /** 表示一个可用的法术条目 */
    private static class SpellEntry {
        final ItemStack pageStack;   // 书签物品
        final MagicPageItem page;     // 页面实例
        final int slot;               // 在容器中的槽位
        final int internalIndex;      // 在页面内的法术索引

        SpellEntry(ItemStack pageStack, MagicPageItem page, int slot, int internalIndex) {
            this.pageStack = pageStack;
            this.page = page;
            this.slot = slot;
            this.internalIndex = internalIndex;
        }
    }

    /**
     * 根据槽位类型构建当前可用的法术列表
     */
    private List<SpellEntry> buildSpellList(ItemStack stack, MagicPageItem.SlotType slotType) {
        List<SpellEntry> list = new ArrayList<>();
        BookInventory inv = getInventory(stack);
        int start = (slotType == MagicPageItem.SlotType.LEFT) ? 0 : inv.getLeftSlots();
        int end = (slotType == MagicPageItem.SlotType.LEFT) ? inv.getLeftSlots() : inv.getSlots();

        for (int slot = start; slot < end; slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof MagicPageItem)) continue;
            MagicPageItem page = (MagicPageItem) pageStack.getItem();
            int spellCount = page.getSpellCount(slotType);
            for (int j = 0; j < spellCount; j++) {
                list.add(new SpellEntry(pageStack, page, slot, j));
            }
        }
        return list;
    }

    /**
     * 确保当前法术索引在合法范围内
     */
    private void validateSpellIndices(ItemStack stack) {
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        boolean dirty = false;

        List<SpellEntry> leftSpells = buildSpellList(stack, MagicPageItem.SlotType.LEFT);
        int curLeft = tag.getInteger(TAG_CUR_LEFT_INDEX);
        if (curLeft < 0 || (leftSpells.isEmpty() && curLeft != 0) || (!leftSpells.isEmpty() && curLeft >= leftSpells.size())) {
            tag.setInteger(TAG_CUR_LEFT_INDEX, 0);
            dirty = true;
        }

        List<SpellEntry> rightSpells = buildSpellList(stack, MagicPageItem.SlotType.RIGHT);
        int curRight = tag.getInteger(TAG_CUR_RIGHT_INDEX);
        if (curRight < 0 || (rightSpells.isEmpty() && curRight != 0) || (!rightSpells.isEmpty() && curRight >= rightSpells.size())) {
            tag.setInteger(TAG_CUR_RIGHT_INDEX, 0);
            dirty = true;
        }

        if (dirty) {
            stack.setTagCompound(tag);
        }
    }

    public boolean executeSpell(ItemStack bookStack, EntityPlayer player, MagicPageItem.SlotType slot, @Nullable Entity target) {
        if (ToolHelper.isBroken(bookStack)) return false;

        validateSpellIndices(bookStack);
        List<SpellEntry> spells = buildSpellList(bookStack, slot);
        if (spells.isEmpty()) return false;

        NBTTagCompound tag = TagUtil.getTagSafe(bookStack);
        int index = tag.getInteger(slot == MagicPageItem.SlotType.LEFT ? TAG_CUR_LEFT_INDEX : TAG_CUR_RIGHT_INDEX);
        if (index < 0 || index >= spells.size()) index = 0;
        SpellEntry entry = spells.get(index);

        NBTTagCompound pageData = entry.pageStack.getTagCompound();
        if (pageData == null) pageData = new NBTTagCompound();
        pageData.setInteger("spellIndex", entry.internalIndex);

        boolean result;
        if (slot == MagicPageItem.SlotType.LEFT) {
            result = entry.page.onLeftClick(bookStack, player, target, pageData, entry.pageStack);
        } else {
            result = entry.page.onRightClick(player.world, player, bookStack, pageData, entry.pageStack);
        }

        if (result) {
            entry.pageStack.setTagCompound(pageData);
            getInventory(bookStack).setStackInSlot(entry.slot, entry.pageStack);
            ToolHelper.damageTool(bookStack, DURABILITY_COST, player);
        }
        return result;
    }

    // ==================== 切换法术 ====================

    public void switchSpell(ItemStack stack, MagicPageItem.SlotType slot, boolean next) {
        validateSpellIndices(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        String key = (slot == MagicPageItem.SlotType.LEFT) ? TAG_CUR_LEFT_INDEX : TAG_CUR_RIGHT_INDEX;
        List<SpellEntry> spells = buildSpellList(stack, slot);
        if (spells.isEmpty()) {
            tag.setInteger(key, 0);
        } else {
            int current = tag.getInteger(key);
            if (current < 0 || current >= spells.size()) current = 0;
            current = next ? (current + 1) % spells.size() : (current - 1 + spells.size()) % spells.size();
            tag.setInteger(key, current);
        }
        stack.setTagCompound(tag);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {

        if (player.isSneaking() || ToolHelper.isBroken(stack)) {
            return true;
        }

        boolean executed = executeSpell(stack, player, MagicPageItem.SlotType.LEFT, entity);

        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (!world.isRemote) {
                player.openGui(TConGreedyAddon.instance, 0, world, hand.ordinal(), 0, 0);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (ToolHelper.isBroken(stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        boolean executed = executeSpell(stack, player, MagicPageItem.SlotType.RIGHT, null);
        if (executed) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        } else {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (!(entity instanceof EntityPlayer) || !isSelected) return;
        EntityPlayer player = (EntityPlayer) entity;

        if (!world.isRemote) {
            cleanupExcessPages(stack, player);
        }

        BookInventory inv = getInventory(stack);
        boolean dirty = false;

        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof MagicPageItem)) continue;
            MagicPageItem page = (MagicPageItem) pageStack.getItem();
            MagicPageItem.SlotType slotType = (slot < inv.getLeftSlots()) ? MagicPageItem.SlotType.LEFT : MagicPageItem.SlotType.RIGHT;

            NBTTagCompound oldData = pageStack.getTagCompound();
            if (oldData == null) oldData = new NBTTagCompound();
            NBTTagCompound newData = oldData.copy();

            // 修改点：传入 pageStack 作为最后一个参数
            page.onHeldUpdate(world, player, stack, newData, slotType, pageStack);
            if (!newData.equals(oldData)) {
                pageStack.setTagCompound(newData);
                inv.setStackInSlot(slot, pageStack);
                dirty = true;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(stack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() >= 4) {
            Material pageMat = materials.get(2);
            Material coreMat = materials.get(3);
            BookPageStats pageStats = pageMat.getStats(BookPageStats.TYPE);
            if (pageStats != null) {
                for (String line : pageStats.getLocalizedInfo()) {
                    tooltip.add(TextFormatting.GOLD + line);
                }
            }
            MagicCoreStats coreStats = coreMat.getStats(MagicCoreStats.TYPE);
            if (coreStats != null) {
                for (String infoLine : coreStats.getLocalizedInfo()) {
                    tooltip.add(TextFormatting.GOLD + infoLine);
                }
            }
        }

        NBTTagCompound tag = TagUtil.getTagSafe(stack);

        List<SpellEntry> leftSpells = buildSpellList(stack, MagicPageItem.SlotType.LEFT);
        int curLeft = tag.getInteger(TAG_CUR_LEFT_INDEX);
        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.leftpage") + ":");
        if (leftSpells.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + "  " + I18n.format("tooltip.empty"));
        } else {
            for (int i = 0; i < leftSpells.size(); i++) {
                SpellEntry entry = leftSpells.get(i);
                String spellName = entry.page.getSpellDisplayName(entry.internalIndex, MagicPageItem.SlotType.LEFT);
                if (i == curLeft) {
                    tooltip.add(TextFormatting.GREEN + "  - " + spellName + " " + I18n.format("tooltip.current"));
                } else {
                    tooltip.add(TextFormatting.GRAY + "  - " + spellName);
                }
            }
        }

        // 右槽法术
        List<SpellEntry> rightSpells = buildSpellList(stack, MagicPageItem.SlotType.RIGHT);
        int curRight = tag.getInteger(TAG_CUR_RIGHT_INDEX);
        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.rightpage") + ":");
        if (rightSpells.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + "  " + I18n.format("tooltip.empty"));
        } else {
            for (int i = 0; i < rightSpells.size(); i++) {
                SpellEntry entry = rightSpells.get(i);
                String spellName = entry.page.getSpellDisplayName(entry.internalIndex, MagicPageItem.SlotType.RIGHT);
                if (i == curRight) {
                    tooltip.add(TextFormatting.GREEN + "  - " + spellName + " " + I18n.format("tooltip.current"));
                } else {
                    tooltip.add(TextFormatting.GRAY + "  - " + spellName);
                }
            }
        }
    }

    // ==================== 工具属性 ====================

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HeadMaterialStats head = materials.get(0).getStatsOrUnknown(HeadMaterialStats.TYPE);
        HandleMaterialStats handle = materials.get(1).getStatsOrUnknown(HandleMaterialStats.TYPE);
        ToolNBT data = new ToolNBT();
        data.head(head);
        data.handle(handle);
        data.attack += 1.0f;
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public float damagePotential() {
        return 0.75F;
    }

    @Override
    public double attackSpeed() {
        return 1.0;
    }

    @Override
    public boolean isEffective(IBlockState state) {
        return false;
    }

    @Override
    public String getIdentifier() {
        return "magicbook";
    }

    // ==================== 辅助方法 ====================

    public static float getBeamRangeFromBook(ItemStack bookStack) {
        if (!(bookStack.getItem() instanceof MagicBook)) return MagicBook.BEAM_RANGE;
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(bookStack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() < 3) return MagicBook.BEAM_RANGE;
        Material coreMat = materials.get(2);
        MagicCoreStats coreStats = coreMat.getStats(MagicCoreStats.TYPE);
        return coreStats != null ? coreStats.range : MagicBook.BEAM_RANGE;
    }

    @Nullable
    private BookPageStats getCoreBookPageStats(ItemStack toolStack) {
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(toolStack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() < 3) return null;
        Material pageMat = materials.get(2);
        return pageMat.getStats(BookPageStats.TYPE);
    }

    public int[] getSlotCounts(ItemStack stack) {
        BookPageStats stats = getCoreBookPageStats(stack);
        int left = (stats != null) ? stats.leftSlots : 1;
        int right = (stats != null) ? stats.rightSlots : 1;
        return new int[]{left, right};
    }

    private void cleanupExcessPages(ItemStack stack, @Nullable EntityPlayer player) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("BookInventory", 10)) return;

        // 获取当前容量
        BookPageStats stats = getCoreBookPageStats(stack);
        int leftSlots = (stats != null) ? stats.leftSlots : 1;
        int rightSlots = (stats != null) ? stats.rightSlots : 1;

        // 检查容量是否变化
        int lastLeft = tag.getInteger("LastLeftSlots");
        int lastRight = tag.getInteger("LastRightSlots");
        if (lastLeft == leftSlots && lastRight == rightSlots) return;

        NBTTagCompound invTag = tag.getCompoundTag("BookInventory");
        List<ItemStack> excess = new ArrayList<>();

        // 处理左槽
        if (invTag.hasKey("Left", 10)) {
            ItemStackHandler tempLeft = new ItemStackHandler(leftSlots);
            tempLeft.deserializeNBT(invTag.getCompoundTag("Left"));
            for (int i = leftSlots; i < tempLeft.getSlots(); i++) {
                ItemStack s = tempLeft.getStackInSlot(i);
                if (!s.isEmpty()) excess.add(s);
            }
            // 截断左槽数据
            NBTTagList leftItems = new NBTTagList();
            for (int i = 0; i < Math.min(tempLeft.getSlots(), leftSlots); i++) {
                ItemStack s = tempLeft.getStackInSlot(i);
                if (!s.isEmpty()) {
                    NBTTagCompound slotTag = new NBTTagCompound();
                    slotTag.setInteger("Slot", i);
                    s.writeToNBT(slotTag);
                    leftItems.appendTag(slotTag);
                }
            }
            NBTTagCompound newLeft = new NBTTagCompound();
            newLeft.setTag("Items", leftItems);
            newLeft.setInteger("Size", leftSlots);
            invTag.setTag("Left", newLeft);
        }

        // 处理右槽
        if (invTag.hasKey("Right", 10)) {
            ItemStackHandler tempRight = new ItemStackHandler(rightSlots);
            tempRight.deserializeNBT(invTag.getCompoundTag("Right"));
            for (int i = rightSlots; i < tempRight.getSlots(); i++) {
                ItemStack s = tempRight.getStackInSlot(i);
                if (!s.isEmpty()) excess.add(s);
            }
            NBTTagList rightItems = new NBTTagList();
            for (int i = 0; i < Math.min(tempRight.getSlots(), rightSlots); i++) {
                ItemStack s = tempRight.getStackInSlot(i);
                if (!s.isEmpty()) {
                    NBTTagCompound slotTag = new NBTTagCompound();
                    slotTag.setInteger("Slot", i);
                    s.writeToNBT(slotTag);
                    rightItems.appendTag(slotTag);
                }
            }
            NBTTagCompound newRight = new NBTTagCompound();
            newRight.setTag("Items", rightItems);
            newRight.setInteger("Size", rightSlots);
            invTag.setTag("Right", newRight);
        }

        // 更新标记
        tag.setInteger("LastLeftSlots", leftSlots);
        tag.setInteger("LastRightSlots", rightSlots);
        tag.setTag("BookInventory", invTag);
        stack.setTagCompound(tag);

        // 弹出多余物品
        if (!excess.isEmpty() && player != null && !player.world.isRemote) {
            for (ItemStack s : excess) {
                player.dropItem(s, false, true);
            }
        }
    }
}