package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.MagicCoreStats;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.ArrayList;
import java.util.List;

public class MagicBook extends TinkerToolCore {

    public static final float BEAM_RANGE = 10.0F;
    public static final int DURABILITY_COST = 1;

    private static final Logger LOGGER = LogManager.getLogger("TConGreedyAddon/magicbook");

    // NBT keys for slots (changed to lists)
    public static final String TAG_LEFT_PAGES = "leftPages";
    public static final String TAG_RIGHT_PAGES = "rightPages";
    public static final String TAG_CUR_LEFT_INDEX = "currentLeftSpellIndex";
    public static final String TAG_CUR_RIGHT_INDEX = "currentRightSpellIndex";
    // Keys used inside each page's compound
    public static final String TAG_PAGE_ID = "pageId";
    public static final String TAG_SPELL_INDEX = "spellIndex"; // internal spell index within the page
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

    /**
     * Initialize slot lists based on material stats.
     * Ensures list length matches max slots, filling empty slots with empty NBTTagCompound.
     */
    private void initSlots(ItemStack stack) {
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        boolean dirty = false;

        BookPageStats stats = getCoreBookPageStats(stack);
        int maxLeft = (stats != null) ? stats.leftSlots : 1;
        int maxRight = (stats != null) ? stats.rightSlots : 1;

        // Left pages
        NBTTagList leftList = tag.getTagList(TAG_LEFT_PAGES, 10);
        // Expand if needed
        while (leftList.tagCount() < maxLeft) {
            leftList.appendTag(new NBTTagCompound());
            dirty = true;
        }
        // Truncate if too many (should not happen, but safe)
        if (leftList.tagCount() > maxLeft) {
            NBTTagList newList = new NBTTagList();
            for (int i = 0; i < maxLeft; i++) {
                newList.appendTag(leftList.get(i));
            }
            tag.setTag(TAG_LEFT_PAGES, newList);
            dirty = true;
        } else if (dirty) {
            tag.setTag(TAG_LEFT_PAGES, leftList);
        }

        // Right pages
        NBTTagList rightList = tag.getTagList(TAG_RIGHT_PAGES, 10);
        while (rightList.tagCount() < maxRight) {
            rightList.appendTag(new NBTTagCompound());
            dirty = true;
        }
        if (rightList.tagCount() > maxRight) {
            NBTTagList newList = new NBTTagList();
            for (int i = 0; i < maxRight; i++) {
                newList.appendTag(rightList.get(i));
            }
            tag.setTag(TAG_RIGHT_PAGES, newList);
            dirty = true;
        } else if (dirty) {
            tag.setTag(TAG_RIGHT_PAGES, rightList);
        }

        if (dirty) {
            stack.setTagCompound(tag);
        }
    }

    /**
     * Represents a single selectable spell from a page.
     */
    private static class SpellEntry {
        final NBTTagCompound pageData;  // The NBT of the page (contains cooldowns, pageId)
        final MagicPageItem page;
        final int internalIndex;        // Index of this spell within the page

        SpellEntry(NBTTagCompound pageData, MagicPageItem page, int internalIndex) {
            this.pageData = pageData;
            this.page = page;
            this.internalIndex = internalIndex;
        }
    }

    /**
     * Build the list of all spells available in the given slot.
     */
    private List<SpellEntry> buildSpellList(ItemStack stack, MagicPageItem.SlotType slot) {
        List<SpellEntry> list = new ArrayList<>();
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        String listKey = (slot == MagicPageItem.SlotType.LEFT) ? TAG_LEFT_PAGES : TAG_RIGHT_PAGES;
        NBTTagList pageList = tag.getTagList(listKey, 10);
        for (int i = 0; i < pageList.tagCount(); i++) {
            NBTTagCompound pageData = pageList.getCompoundTagAt(i);
            if (pageData.isEmpty()) continue; // empty slot
            String pageId = pageData.getString(TAG_PAGE_ID);
            Item item = Item.REGISTRY.getObject(new ResourceLocation(pageId));
            if (item instanceof MagicPageItem) {
                MagicPageItem page = (MagicPageItem) item;
                int spellCount = page.getSpellCount(slot); // need to add this method in MagicPageItem
                for (int j = 0; j < spellCount; j++) {
                    list.add(new SpellEntry(pageData, page, j));
                }
            }
        }
        return list;
    }

    /**
     * Ensure current spell indices are within bounds.
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

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        initSlots(stack);
        validateSpellIndices(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        List<SpellEntry> spells = buildSpellList(stack, MagicPageItem.SlotType.LEFT);
        if (spells.isEmpty()) return true; // no spells to cast

        int index = tag.getInteger(TAG_CUR_LEFT_INDEX);
        if (index < 0 || index >= spells.size()) index = 0;
        SpellEntry entry = spells.get(index);

        // Check cooldown
        if (isSpellOnCooldown(player.world, entry)) return true;

        // Temporarily set the spell index in the page data (some pages may rely on it)
        entry.pageData.setInteger(TAG_SPELL_INDEX, entry.internalIndex);
        boolean result = entry.page.onLeftClick(stack, player, entity, entry.pageData);
        if (result) {
            setSpellCooldown(player.world, entry);
            // Write back the page data to the list
            NBTTagList leftList = tag.getTagList(TAG_LEFT_PAGES, 10);
            // Find the index of this page in the list (by matching the compound? we can use the same slot? but pageData may have been modified)
            // Simpler: since we have the exact pageData object, we need to know its position in the list.
            // We can loop to find a matching reference? Better: store slot index in SpellEntry.
            // For simplicity, we can add slotIndex to SpellEntry when building, but that requires passing the list index.
            // Alternative: after calling onLeftClick, we don't need to write back the whole pageData if the page modified it,
            // because pageData is a reference to the actual compound in the list (since we got it from the list's getCompoundTagAt, which returns a reference? In Forge NBTTagList.getCompoundTagAt returns a copy? Actually it returns the tag at index, but if you modify it, the list is updated. So we can just rely on that.
            // However, to be safe, we can mark the list as changed.
            tag.setTag(TAG_LEFT_PAGES, leftList); // this might be redundant if leftList is the same object
            stack.setTagCompound(tag);
            ToolHelper.damageTool(stack, DURABILITY_COST, player);
        }
        return result;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // Sneak-right-click with a page in offhand to install
        ItemStack offhand = player.getHeldItemOffhand();
        if (hand == EnumHand.MAIN_HAND && player.isSneaking() && !offhand.isEmpty() && offhand.getItem() instanceof MagicPageItem) {
            if (world.isRemote) {
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            } else {
                return handlePageInstallation(world, player, stack, offhand);
            }
        }

        if (ToolHelper.isBroken(stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        initSlots(stack);
        validateSpellIndices(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        List<SpellEntry> spells = buildSpellList(stack, MagicPageItem.SlotType.RIGHT);
        if (spells.isEmpty()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        int index = tag.getInteger(TAG_CUR_RIGHT_INDEX);
        if (index < 0 || index >= spells.size()) index = 0;
        SpellEntry entry = spells.get(index);

        if (world.isRemote) {
            // Client side: just indicate success if not on cooldown? But we need server to actually cast.
            // For simplicity, return SUCCESS if can cast (client can't check cooldown? but we can approximate)
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        } else {
            if (isSpellOnCooldown(world, entry)) {
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            }
            entry.pageData.setInteger(TAG_SPELL_INDEX, entry.internalIndex);
            boolean result = entry.page.onRightClick(world, player, stack, entry.pageData);
            if (result) {
                setSpellCooldown(world, entry);
                // Update list (similar to left click)
                NBTTagList rightList = tag.getTagList(TAG_RIGHT_PAGES, 10);
                tag.setTag(TAG_RIGHT_PAGES, rightList);
                stack.setTagCompound(tag);
                ToolHelper.damageTool(stack, DURABILITY_COST, player);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
    }

    /**
     * Install a page into the book.
     */
    private ActionResult<ItemStack> handlePageInstallation(World world, EntityPlayer player, ItemStack toolStack, ItemStack pageStack) {
        MagicPageItem page = (MagicPageItem) pageStack.getItem();
        MagicPageItem.SlotType slotType = page.getSlotType();
        BookPageStats stats = getCoreBookPageStats(toolStack);
        int maxSlots = (slotType == MagicPageItem.SlotType.LEFT) ? (stats != null ? stats.leftSlots : 1) : (stats != null ? stats.rightSlots : 1);
        if (maxSlots <= 0) {
            if (!world.isRemote) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + I18n.format("message.slot_unavailable")));
            }
            return new ActionResult<>(EnumActionResult.FAIL, toolStack);
        }

        String targetListKey = (slotType == MagicPageItem.SlotType.LEFT) ? TAG_LEFT_PAGES : TAG_RIGHT_PAGES;
        NBTTagCompound toolTag = TagUtil.getTagSafe(toolStack);
        NBTTagList pageList = toolTag.getTagList(targetListKey, 10);

        while (pageList.tagCount() < maxSlots) {
            pageList.appendTag(new NBTTagCompound());
        }

        String newPageId = page.getPageIdentifier();
        for (int i = 0; i < pageList.tagCount(); i++) {
            NBTTagCompound existing = pageList.getCompoundTagAt(i);
            if (!existing.isEmpty()) {
                String existingId = existing.getString(TAG_PAGE_ID);
                if (existingId.equals(newPageId)) {
                    if (!world.isRemote) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + I18n.format("message.duplicate_page")));
                    }
                    return new ActionResult<>(EnumActionResult.FAIL, toolStack);
                }
            }
        }

        int targetIndex = -1;
        for (int i = 0; i < pageList.tagCount(); i++) {
            if (pageList.getCompoundTagAt(i).isEmpty()) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            if (!world.isRemote) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + I18n.format("message.slot_full")));
            }
            return new ActionResult<>(EnumActionResult.FAIL, toolStack);
        }

        NBTTagCompound oldData = pageList.getCompoundTagAt(targetIndex);
        if (!oldData.isEmpty() && !player.capabilities.isCreativeMode) {
            String oldPageId = oldData.getString(TAG_PAGE_ID);
            Item oldPageItem = Item.REGISTRY.getObject(new ResourceLocation(oldPageId));
            if (oldPageItem != null) {
                ItemStack oldStack = new ItemStack(oldPageItem);
                if (!player.inventory.addItemStackToInventory(oldStack)) {
                    player.dropItem(oldStack, false);
                }
            }
        }

        // 创建新页面数据
        NBTTagCompound newData = new NBTTagCompound();
        newData.setString(TAG_PAGE_ID, newPageId);
        newData.setTag(TAG_COOLDOWNS, new NBTTagCompound());

        pageList.set(targetIndex, newData);
        toolTag.setTag(targetListKey, pageList);
        toolStack.setTagCompound(toolTag);

        if (!player.capabilities.isCreativeMode) {
            pageStack.shrink(1);
        }
        if (!world.isRemote) {
            player.sendMessage(new TextComponentString(I18n.format("page.installed", I18n.format(slotType == MagicPageItem.SlotType.LEFT ? "slot.left" : "slot.right"))));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, toolStack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;
        if (!isSelected) return;

        initSlots(stack);
        validateSpellIndices(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        boolean dirty = false;

        // Update left pages
        NBTTagList leftList = tag.getTagList(TAG_LEFT_PAGES, 10);
        for (int i = 0; i < leftList.tagCount(); i++) {
            NBTTagCompound pageData = leftList.getCompoundTagAt(i);
            if (pageData.isEmpty()) continue;
            String pageId = pageData.getString(TAG_PAGE_ID);
            Item item = Item.REGISTRY.getObject(new ResourceLocation(pageId));
            if (item instanceof MagicPageItem) {
                NBTTagCompound copy = pageData.copy();
                ((MagicPageItem) item).onHeldUpdate(world, player, stack, copy, MagicPageItem.SlotType.LEFT);
                if (!copy.equals(pageData)) {
                    leftList.set(i, copy);
                    dirty = true;
                }
            }
        }

        // Update right pages
        NBTTagList rightList = tag.getTagList(TAG_RIGHT_PAGES, 10);
        for (int i = 0; i < rightList.tagCount(); i++) {
            NBTTagCompound pageData = rightList.getCompoundTagAt(i);
            if (pageData.isEmpty()) continue;
            String pageId = pageData.getString(TAG_PAGE_ID);
            Item item = Item.REGISTRY.getObject(new ResourceLocation(pageId));
            if (item instanceof MagicPageItem) {
                NBTTagCompound copy = pageData.copy();
                ((MagicPageItem) item).onHeldUpdate(world, player, stack, copy, MagicPageItem.SlotType.RIGHT);
                if (!copy.equals(pageData)) {
                    rightList.set(i, copy);
                    dirty = true;
                }
            }
        }

        if (dirty) {
            tag.setTag(TAG_LEFT_PAGES, leftList);
            tag.setTag(TAG_RIGHT_PAGES, rightList);
            stack.setTagCompound(tag);
        }
    }

    /**
     * Switch to next/previous spell in the given slot.
     * This method should be called from an external key handler.
     */
    public void switchSpell(ItemStack stack, MagicPageItem.SlotType slot, boolean next) {
        initSlots(stack);
        validateSpellIndices(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        String key = (slot == MagicPageItem.SlotType.LEFT) ? TAG_CUR_LEFT_INDEX : TAG_CUR_RIGHT_INDEX;
        int current = tag.getInteger(key);
        List<SpellEntry> spells = buildSpellList(stack, slot);
        if (spells.isEmpty()) {
            tag.setInteger(key, 0);
        } else {
            int max = spells.size();
            current = next ? (current + 1) % max : (current - 1 + max) % max;
            tag.setInteger(key, current);
        }
        stack.setTagCompound(tag);
    }

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

        // Left slot spells
        List<SpellEntry> leftSpells = buildSpellList(stack, MagicPageItem.SlotType.LEFT);
        int curLeft = tag.getInteger(TAG_CUR_LEFT_INDEX);
        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.leftpage") + ":");
        if (leftSpells.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + "  " + I18n.format("tooltip.empty"));
        } else {
            for (int i = 0; i < leftSpells.size(); i++) {
                SpellEntry entry = leftSpells.get(i);
                String spellName = entry.page.getSpellDisplayName(entry.internalIndex, MagicPageItem.SlotType.LEFT); // need new method
                if (i == curLeft) {
                    tooltip.add(TextFormatting.GREEN + "  - " + spellName + " " + I18n.format("tooltip.current"));
                } else {
                    tooltip.add(TextFormatting.GRAY + "  - " + spellName);
                }
            }
        }

        // Right slot spells
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

    // Cooldown helpers
    private boolean isSpellOnCooldown(World world, SpellEntry entry) {
        NBTTagCompound cooldowns = entry.pageData.getCompoundTag(TAG_COOLDOWNS);
        long lastUsed = cooldowns.getLong(String.valueOf(entry.internalIndex));
        int cooldown = entry.page.getSpellCooldownTicks(entry.internalIndex, entry.page.getSlotType()); // need new method
        if (cooldown <= 0) return false;
        long now = world.getTotalWorldTime();
        return now - lastUsed < cooldown;
    }

    private void setSpellCooldown(World world, SpellEntry entry) {
        int cooldown = entry.page.getSpellCooldownTicks(entry.internalIndex, entry.page.getSlotType());
        if (cooldown > 0) {
            NBTTagCompound cooldowns = entry.pageData.getCompoundTag(TAG_COOLDOWNS);
            cooldowns.setLong(String.valueOf(entry.internalIndex), world.getTotalWorldTime());
            entry.pageData.setTag(TAG_COOLDOWNS, cooldowns);
        }
    }

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
        // BookPageStats is from the third material (bookpage)
        Material pageMat = materials.get(2);
        return pageMat.getStats(BookPageStats.TYPE);
    }

}