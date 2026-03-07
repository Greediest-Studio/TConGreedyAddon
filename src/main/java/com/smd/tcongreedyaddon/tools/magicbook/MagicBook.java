package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import com.smd.tcongreedyaddon.plugin.oldweapons.OldWeapons;
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
import slimeknights.tconstruct.library.Util;
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
import java.util.List;

public class MagicBook extends TinkerToolCore {

    public static final float BEAM_RANGE = 10.0F;
    public static final int DURABILITY_COST = 1;

    private static final Logger LOGGER = LogManager.getLogger("TConGreedyAddon/magicbook");

    // NBT keys for slots
    private static final String TAG_LEFT_PAGE = "leftPage";
    public static final String TAG_RIGHT_PAGE = "rightPage";
    public static final String TAG_PAGE_ID = "pageId";
    public static final String TAG_SPELL_INDEX = "spellIndex";

    public MagicBook() {
        super(
                PartMaterialType.head(SpecialWeapons.cover),
                PartMaterialType.handle(SpecialWeapons.hinge),
                new PartMaterialType(SpecialWeapons.bookpage, SlotStats.TYPE),
                new PartMaterialType(SpecialWeapons.magiccore, RangeMaterialStats.TYPE)
        );
        addCategory(Category.WEAPON);
        setTranslationKey("magicbook").setRegistryName("magicbook");
    }

    private void initSlots(ItemStack stack) {
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        boolean dirty = false;

        SlotStats stats = getCoreSlotStats(stack);
        boolean hasLeft = true, hasRight = true;

        if (stats != null) {
            hasLeft = stats.hasLeft;
            hasRight = stats.hasRight;
        } else {
            LOGGER.info("No SlotStats found for core material, defaulting to both slots available.");
        }

        if (hasLeft && !tag.hasKey(TAG_LEFT_PAGE)) {
            tag.setTag(TAG_LEFT_PAGE, new NBTTagCompound());
            dirty = true;
        }

        else if (!hasLeft && tag.hasKey(TAG_LEFT_PAGE)) {
            tag.removeTag(TAG_LEFT_PAGE);
            dirty = true;
        }

        if (hasRight && !tag.hasKey(TAG_RIGHT_PAGE)) {
            tag.setTag(TAG_RIGHT_PAGE, new NBTTagCompound());
            dirty = true;
        }
        else if (!hasRight && tag.hasKey(TAG_RIGHT_PAGE)) {
            tag.removeTag(TAG_RIGHT_PAGE);
            dirty = true;
        }

        if (dirty) {
            stack.setTagCompound(tag);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        initSlots(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        NBTTagCompound leftData = tag.getCompoundTag(TAG_LEFT_PAGE);
        if (!leftData.isEmpty()) {
            String pageId = leftData.getString(TAG_PAGE_ID);
            Item pageItem = Item.REGISTRY.getObject(new ResourceLocation(pageId));
            if (pageItem instanceof MagicPageItem) {
                MagicPageItem page = (MagicPageItem) pageItem;
                return page.onLeftClick(stack, player, entity);
            }
        }
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

        ItemStack offhand = player.getHeldItemOffhand();
        if (hand == EnumHand.MAIN_HAND && !offhand.isEmpty() && offhand.getItem() instanceof MagicPageItem) {
            MagicPageItem page = (MagicPageItem) offhand.getItem();
            MagicPageItem.SlotType slotType = page.getSlotType();
            boolean slotAvailable = isSlotAvailable(stack, slotType);

            if (world.isRemote) {
                return new ActionResult<>(slotAvailable ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
            } else {
                if (slotAvailable) {
                    return handlePageInstallation(world, player, stack, offhand);
                } else {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + I18n.format("message.slot_unavailable")));
                    return new ActionResult<>(EnumActionResult.FAIL, stack);
                }
            }
        }

        if (player.isSneaking()) {
            initSlots(stack);
            NBTTagCompound tag = TagUtil.getTagSafe(stack);
            NBTTagCompound rightData = tag.getCompoundTag(TAG_RIGHT_PAGE);
            boolean hasRightPage = !rightData.isEmpty();

            if (world.isRemote) {
                return new ActionResult<>(hasRightPage ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
            } else {
                if (hasRightPage) {
                    String pageId = rightData.getString(TAG_PAGE_ID);
                    Item pageItem = Item.REGISTRY.getObject(new ResourceLocation(pageId));
                    if (pageItem instanceof MagicPageItem) {
                        MagicPageItem page = (MagicPageItem) pageItem;
                        page.nextSpell(stack, rightData);
                        tag.setTag(TAG_RIGHT_PAGE, rightData);
                        stack.setTagCompound(tag);
                        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                    }
                }
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            }
        }

        if (ToolHelper.isBroken(stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        initSlots(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        NBTTagCompound rightData = tag.getCompoundTag(TAG_RIGHT_PAGE);
        boolean canUseSpell = false;
        MagicPageItem page = null;
        int spellIndex = -1;

        if (!rightData.isEmpty()) {
            String pageId = rightData.getString(TAG_PAGE_ID);
            Item pageItem = Item.REGISTRY.getObject(new ResourceLocation(pageId));
            if (pageItem instanceof MagicPageItem) {
                page = (MagicPageItem) pageItem;
                spellIndex = rightData.getInteger(TAG_SPELL_INDEX);
                canUseSpell = !isPageOnCooldown(world, rightData, page, spellIndex);
            }
        }

        if (world.isRemote) {
            return new ActionResult<>(canUseSpell ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
        } else {
            if (canUseSpell) {
                if (page.onRightClick(world, player, stack, rightData)) {
                    setPageCooldown(world, rightData, page, spellIndex);
                    tag.setTag(TAG_RIGHT_PAGE, rightData);
                    stack.setTagCompound(tag);
                    ToolHelper.damageTool(stack, DURABILITY_COST, player);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;
        if (!isSelected) return;

        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        boolean dirty = false;

        NBTTagCompound leftData = tag.getCompoundTag(TAG_LEFT_PAGE);
        if (!leftData.isEmpty()) {
            String leftPageId = leftData.getString(TAG_PAGE_ID);
            Item leftPageItem = Item.REGISTRY.getObject(new ResourceLocation(leftPageId));
            if (leftPageItem instanceof MagicPageItem) {
                NBTTagCompound leftCopy = leftData.copy();
                ((MagicPageItem) leftPageItem).onHeldUpdate(world, player, stack, leftCopy, MagicPageItem.SlotType.LEFT);
                if (!leftCopy.equals(leftData)) {
                    tag.setTag(TAG_LEFT_PAGE, leftCopy);
                    dirty = true;
                }
            }
        }

        NBTTagCompound rightData = tag.getCompoundTag(TAG_RIGHT_PAGE);
        if (!rightData.isEmpty()) {
            String rightPageId = rightData.getString(TAG_PAGE_ID);
            Item rightPageItem = Item.REGISTRY.getObject(new ResourceLocation(rightPageId));
            if (rightPageItem instanceof MagicPageItem) {
                NBTTagCompound rightCopy = rightData.copy();
                ((MagicPageItem) rightPageItem).onHeldUpdate(world, player, stack, rightCopy, MagicPageItem.SlotType.RIGHT);
                if (!rightCopy.equals(rightData)) {
                    tag.setTag(TAG_RIGHT_PAGE, rightCopy);
                    dirty = true;
                }
            }
        }
        if (dirty) {
            stack.setTagCompound(tag);
        }
    }

    private ActionResult<ItemStack> handlePageInstallation(World world, EntityPlayer player, ItemStack toolStack, ItemStack pageStack) {
        MagicPageItem page = (MagicPageItem) pageStack.getItem();
        MagicPageItem.SlotType slotType = page.getSlotType();

        if (!isSlotAvailable(toolStack, slotType)) {
            if (!world.isRemote) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + I18n.format("message.slot_unavailable")));
            }
            return new ActionResult<>(EnumActionResult.FAIL, toolStack);
        }

        String targetSlot = slotType == MagicPageItem.SlotType.LEFT ? TAG_LEFT_PAGE : TAG_RIGHT_PAGE;

        NBTTagCompound toolTag = TagUtil.getTagSafe(toolStack);
        NBTTagCompound slotData = toolTag.getCompoundTag(targetSlot);

        if (!slotData.isEmpty() && !player.capabilities.isCreativeMode) {
            String oldPageId = slotData.getString(TAG_PAGE_ID);
            Item oldPageItem = Item.REGISTRY.getObject(new ResourceLocation(oldPageId));
            if (oldPageItem != null) {
                ItemStack oldStack = new ItemStack(oldPageItem);
                if (!player.inventory.addItemStackToInventory(oldStack)) {
                    player.dropItem(oldStack, false);
                }
            }
        }

        NBTTagCompound newData = new NBTTagCompound();
        newData.setString(TAG_PAGE_ID, page.getPageIdentifier());
        if (targetSlot.equals(TAG_RIGHT_PAGE)) {
            newData.setInteger(TAG_SPELL_INDEX, page.getInitialSpellIndex(pageStack));
            newData.setTag("cooldowns", new NBTTagCompound());
        }
        toolTag.setTag(targetSlot, newData);
        toolStack.setTagCompound(toolTag);

        if (!player.capabilities.isCreativeMode) {
            pageStack.shrink(1);
        }

        player.sendMessage(new TextComponentString(I18n.format("page_installed_to") + (slotType == MagicPageItem.SlotType.LEFT ? I18n.format("left") : I18n.format("right")) + I18n.format("slot")));
        return new ActionResult<>(EnumActionResult.SUCCESS, toolStack);
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HeadMaterialStats head = materials.get(0).getStatsOrUnknown(HeadMaterialStats.TYPE);
        HandleMaterialStats handle = materials.get(1).getStatsOrUnknown(HandleMaterialStats.TYPE);
        ExtraMaterialStats extra = materials.get(2).getStatsOrUnknown(ExtraMaterialStats.TYPE);

        ToolNBT data = new ToolNBT();
        data.head(head);
        data.handle(handle);
        data.extra(extra);
        data.attack += 1.0f;
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public float damagePotential() {
        return 0.8F;
    }

    @Override
    public double attackSpeed() {
        return 0.5;
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
        initSlots(stack);
        super.addInformation(stack, worldIn, tooltip, flagIn);

        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(stack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() >= 3) {
            Material coreMat = materials.get(2);
            RangeMaterialStats rangeStats = coreMat.getStats(RangeMaterialStats.TYPE);
            if (rangeStats != null) {
                tooltip.add(TextFormatting.GOLD + Util.translate("stat.range.name") + ": " + rangeStats.range);
            }
        }

        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        if (tag.hasKey(TAG_LEFT_PAGE)) {
            NBTTagCompound left = tag.getCompoundTag(TAG_LEFT_PAGE);
            if (!left.isEmpty()) {
                String pageId = left.getString(TAG_PAGE_ID);
                Item pageItem = Item.REGISTRY.getObject(new ResourceLocation(pageId));
                if (pageItem instanceof MagicPageItem) {
                    MagicPageItem page = (MagicPageItem) pageItem;
                    List<String> spellNames = page.getAllSpellNames(left);
                    if (!spellNames.isEmpty()) {
                        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.leftpage") + ":");
                        String currentSpell = page.getCurrentSpellDisplayName(left);
                        for (String spellName : spellNames) {
                            if (spellName.equals(currentSpell)) {
                                tooltip.add(TextFormatting.GREEN + "  - " + spellName + " " + I18n.format("tooltip.current"));
                            } else {
                                tooltip.add(TextFormatting.GRAY + "  - " + spellName);
                            }
                        }
                    } else {
                        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.leftpage") + ": " + page.getCurrentSpellDisplayName(left));
                    }
                } else {
                    tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.leftpage") + ": " + left.getString(TAG_PAGE_ID));
                }
            } else {
                tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.leftpage") + ": " + I18n.format("tooltip.empty"));
            }
        } else {
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.leftpage") + ": " + I18n.format("tooltip.unavailable"));
        }

        if (tag.hasKey(TAG_RIGHT_PAGE)) {
            NBTTagCompound right = tag.getCompoundTag(TAG_RIGHT_PAGE);
            if (!right.isEmpty()) {
                String pageId = right.getString(TAG_PAGE_ID);
                Item pageItem = Item.REGISTRY.getObject(new ResourceLocation(pageId));
                if (pageItem instanceof MagicPageItem) {
                    MagicPageItem page = (MagicPageItem) pageItem;
                    List<String> spellNames = page.getAllSpellNames(right);
                    if (!spellNames.isEmpty()) {
                        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.rightpage") + ":");
                        String currentSpell = page.getCurrentSpellDisplayName(right);
                        for (String spellName : spellNames) {
                            if (spellName.equals(currentSpell)) {
                                tooltip.add(TextFormatting.GREEN + "  - " + spellName + " " + I18n.format("tooltip.current"));
                            } else {
                                tooltip.add(TextFormatting.GRAY + "  - " + spellName);
                            }
                        }
                    } else {
                        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.rightpage") + ": " + page.getCurrentSpellDisplayName(right));
                    }
                } else {
                    tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.rightpage") + ": " + right.getString(TAG_PAGE_ID));
                }
            } else {
                tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.rightpage") + ":" + I18n.format("tooltip.empty"));
            }
        } else {
            // 没有右槽时显示不可用
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.rightpage") + ": " + I18n.format("tooltip.unavailable"));
        }
    }

    protected float getToolAttackDamage(ItemStack toolStack) {
        return ToolHelper.getActualAttack(toolStack);
    }

    protected boolean performStandardAttack(ItemStack toolStack, EntityPlayer player, Entity target) {
        if (toolStack.getItem() instanceof TinkerToolCore) {
            return ToolHelper.attackEntity(toolStack, (TinkerToolCore) toolStack.getItem(), player, target);
        }
        return false;
    }

    private boolean isPageOnCooldown(World world, NBTTagCompound pageData, MagicPageItem page, int spellIndex) {
        int cooldownTicks = page.getSpellCooldownTicks(spellIndex);
        if (cooldownTicks <= 0) return false;
        NBTTagCompound cooldowns = pageData.getCompoundTag("cooldowns");
        long lastUsed = cooldowns.getLong(String.valueOf(spellIndex));
        long now = world.getTotalWorldTime();
        return now - lastUsed < cooldownTicks;
    }

    private void setPageCooldown(World world, NBTTagCompound pageData, MagicPageItem page, int spellIndex) {
        int cooldownTicks = page.getSpellCooldownTicks(spellIndex);
        if (cooldownTicks > 0) {
            NBTTagCompound cooldowns = pageData.getCompoundTag("cooldowns");
            cooldowns.setLong(String.valueOf(spellIndex), world.getTotalWorldTime());
            pageData.setTag("cooldowns", cooldowns);
        }
    }

    public static float getBeamRangeFromBook(ItemStack bookStack) {
        if (!(bookStack.getItem() instanceof MagicBook)) return MagicBook.BEAM_RANGE;
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(bookStack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() < 3) return MagicBook.BEAM_RANGE;
        Material coreMat = materials.get(2);
        RangeMaterialStats rangeStats = coreMat.getStats(RangeMaterialStats.TYPE);
        return rangeStats != null ? rangeStats.range : MagicBook.BEAM_RANGE;
    }

    @Nullable
    private SlotStats getCoreSlotStats(ItemStack toolStack) {
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(toolStack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() < 3) return null;
        Material coreMat = materials.get(2);
        return coreMat.getStats(SlotStats.TYPE);
    }

    private boolean isSlotAvailable(ItemStack toolStack, MagicPageItem.SlotType slotType) {
        SlotStats stats = getCoreSlotStats(toolStack);
        if (stats == null) {
            return true;
        }
        return slotType == MagicPageItem.SlotType.LEFT ? stats.hasLeft : stats.hasRight;
    }
}