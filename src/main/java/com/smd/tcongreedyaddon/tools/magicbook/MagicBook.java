package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import com.smd.tcongreedyaddon.tools.magicbook.gui.BookInventory;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.MagicCoreStats;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MagicBook extends TinkerToolCore {

    public static final float BEAM_RANGE = 10.0F;
    public static final int DURABILITY_COST = 1;

    private final MagicBookStateHelper stateHelper;
    private final MagicBookCastingCore castingCore;

    public MagicBook() {
        super(
                PartMaterialType.head(SpecialWeapons.cover),
                PartMaterialType.handle(SpecialWeapons.hinge),
                TConGreedyTypes.bookpage(SpecialWeapons.bookpage),
                TConGreedyTypes.magiccore(SpecialWeapons.magiccore)
        );
        addCategory(Category.WEAPON);
        setTranslationKey("magicbook").setRegistryName("magicbook");
        this.stateHelper = new MagicBookStateHelper(this);
        this.castingCore = new MagicBookCastingCore(this, stateHelper);
    }

    public BookInventory getInventory(ItemStack stack) {
        return stateHelper.getInventory(stack);
    }

    public void switchSpell(ItemStack stack, MagicPageItem.SlotType slotType, boolean next) {
        stateHelper.switchSpell(stack, slotType, next);
    }

    public boolean hasSpell(ItemStack bookStack, String spellRegistryName) {
        return stateHelper.hasSpell(bookStack, spellRegistryName);
    }

    public boolean triggerKeybindSkill(ItemStack bookStack, EntityPlayer player, String keyId,
                                       com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindSkillSpell.KeyAction action) {
        return castingCore.triggerKeybindSkill(bookStack, player, keyId, action);
    }

    public int[] getSlotCounts(ItemStack stack) {
        return stateHelper.getSlotCounts(stack);
    }

    @Nullable
    public MagicBookStateHelper.HoldDisplayInfo getSelectedHoldDisplayInfo(ItemStack stack, MagicPageItem.SlotType slotType,
                                                                           EntityPlayer player) {
        return stateHelper.getSelectedHoldDisplayInfo(stack, slotType, player);
    }

    public static boolean isClientHoldActive(EntityPlayer player) {
        return MagicBookStateHelper.isClientHoldActive(player);
    }

    public static int getClientHoldTicks(EntityPlayer player) {
        return MagicBookStateHelper.getClientHoldTicks(player);
    }

    public static void clearClientHoldState(EntityPlayer player) {
        MagicBookStateHelper.clearClientHoldState(player);
    }

    @Nullable
    public static MagicBookStateHelper.HoldDisplayInfo getSelectedMainHandHoldDisplayInfo(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof MagicBook)) {
            return null;
        }
        MagicBook book = (MagicBook) mainHand.getItem();
        return book.getSelectedHoldDisplayInfo(mainHand, MagicPageItem.SlotType.RIGHT, player);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return castingCore.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        return castingCore.onItemRightClick(world, player, hand);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        castingCore.onUsingTick(stack, living, count);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        castingCore.onPlayerStoppedUsing(stack, world, living, timeLeft);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (!(entity instanceof EntityPlayer) || !isSelected) {
            return;
        }
        stateHelper.onSelectedUpdate(stack, world, (EntityPlayer) entity);
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

    public static float getBeamRangeFromBook(ItemStack bookStack) {
        if (!(bookStack.getItem() instanceof MagicBook)) {
            return MagicBook.BEAM_RANGE;
        }
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(bookStack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() < 3) {
            return MagicBook.BEAM_RANGE;
        }
        Material coreMat = materials.get(2);
        MagicCoreStats coreStats = coreMat.getStats(MagicCoreStats.TYPE);
        return coreStats != null ? coreStats.range : MagicBook.BEAM_RANGE;
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

        List<MagicBookStateHelper.SpellEntry> leftSpells = stateHelper.buildSpellList(stack, MagicPageItem.SlotType.LEFT);
        int curLeft = tag.getInteger(MagicBookKeys.TAG_CUR_LEFT_INDEX);
        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.leftpage") + ":");
        if (leftSpells.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + "  " + I18n.format("tooltip.empty"));
        } else {
            for (int i = 0; i < leftSpells.size(); i++) {
                MagicBookStateHelper.SpellEntry entry = leftSpells.get(i);
                String spellName = entry.page.getSpellDisplayName(entry.internalIndex, MagicPageItem.SlotType.LEFT);
                if (i == curLeft) {
                    tooltip.add(TextFormatting.GREEN + "  - " + spellName + " " + I18n.format("tooltip.current"));
                } else {
                    tooltip.add(TextFormatting.GRAY + "  - " + spellName);
                }
            }
        }

        List<MagicBookStateHelper.SpellEntry> rightSpells = stateHelper.buildSpellList(stack, MagicPageItem.SlotType.RIGHT);
        int curRight = tag.getInteger(MagicBookKeys.TAG_CUR_RIGHT_INDEX);
        tooltip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.rightpage") + ":");
        if (rightSpells.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + "  " + I18n.format("tooltip.empty"));
        } else {
            for (int i = 0; i < rightSpells.size(); i++) {
                MagicBookStateHelper.SpellEntry entry = rightSpells.get(i);
                String spellName = entry.page.getSpellDisplayName(entry.internalIndex, MagicPageItem.SlotType.RIGHT);
                if (i == curRight) {
                    tooltip.add(TextFormatting.GREEN + "  - " + spellName + " " + I18n.format("tooltip.current"));
                } else {
                    tooltip.add(TextFormatting.GRAY + "  - " + spellName);
                }
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && !oldStack.isEmpty() && !newStack.isEmpty()
                && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
