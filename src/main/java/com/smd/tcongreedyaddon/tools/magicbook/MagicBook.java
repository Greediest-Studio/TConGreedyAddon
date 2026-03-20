package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import com.smd.tcongreedyaddon.tools.magicbook.gui.BookInventory;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.MagicCoreStats;
import com.smd.tcongreedyaddon.tools.magicbook.page.UnifiedMagicPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IChannelReleaseSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindSkillSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IHoldTriggerSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MagicBook extends TinkerToolCore {

    public static final float BEAM_RANGE = 10.0F;
    public static final int DURABILITY_COST = 1;

    private final Map<Integer, WeakReference<BookInventory>> inventoryCache = new ConcurrentHashMap<>();
    private static final Map<String, HoldRuntimeState> SERVER_HOLD_STATES = new ConcurrentHashMap<>();
    private static final Map<String, HoldRuntimeState> CLIENT_HOLD_STATES = new ConcurrentHashMap<>();

    // NBT keys
    public static final String TAG_CUR_LEFT_INDEX = "currentLeftSpellIndex";
    public static final String TAG_CUR_RIGHT_INDEX = "currentRightSpellIndex";
    public static final String TAG_COOLDOWNS = "cooldowns";

    private static final String HOLD_MODE_CHANNEL = "channel";
    private static final String HOLD_MODE_TRIGGER = "trigger";

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
        int key = System.identityHashCode(stack);
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
        inventoryCache.entrySet().removeIf(entry -> entry.getValue().get() == null);
        return inv;
    }

    /** 表示一个可用的法术条目 */
    private static class SpellEntry {
        final ItemStack pageStack;
        final MagicPageItem page;
        final int slot;
        final int internalIndex;

        SpellEntry(ItemStack pageStack, MagicPageItem page, int slot, int internalIndex) {
            this.pageStack = pageStack;
            this.page = page;
            this.slot = slot;
            this.internalIndex = internalIndex;
        }
    }

    private static class HoldSpellTarget {
        final UnifiedMagicPage page;
        final ItemStack pageStack;
        final int pageSlot;
        final ISpell spell;
        final int rawIndex;
        final NBTTagCompound pageData;

        HoldSpellTarget(UnifiedMagicPage page, ItemStack pageStack, int pageSlot,
                        ISpell spell, int rawIndex, NBTTagCompound pageData) {
            this.page = page;
            this.pageStack = pageStack;
            this.pageSlot = pageSlot;
            this.spell = spell;
            this.rawIndex = rawIndex;
            this.pageData = pageData;
        }
    }

    private static class KeybindSkillTarget {
        final UnifiedMagicPage page;
        final ItemStack pageStack;
        final int pageSlot;
        final IKeybindSkillSpell spell;
        final int rawIndex;
        final MagicPageItem.SlotType slotType;
        final NBTTagCompound pageData;

        private KeybindSkillTarget(UnifiedMagicPage page, ItemStack pageStack, int pageSlot,
                                   IKeybindSkillSpell spell, int rawIndex,
                                   MagicPageItem.SlotType slotType, NBTTagCompound pageData) {
            this.page = page;
            this.pageStack = pageStack;
            this.pageSlot = pageSlot;
            this.spell = spell;
            this.rawIndex = rawIndex;
            this.slotType = slotType;
            this.pageData = pageData;
        }
    }

    private static class HoldRuntimeState {
        final int pageSlot;
        final int rawIndex;
        final String mode;
        final EnumHand hand;
        final long startWorldTick;
        int heldTicks;

        HoldRuntimeState(int pageSlot, int rawIndex, String mode, EnumHand hand, long startWorldTick) {
            this.pageSlot = pageSlot;
            this.rawIndex = rawIndex;
            this.mode = mode;
            this.hand = hand;
            this.startWorldTick = startWorldTick;
        }
    }

    public static final class HoldDisplayInfo {
        public final int chargeTicks;
        public final int maxHoldTicks;
        public final boolean triggerMode;
        public final int pageSlot;
        public final int rawIndex;

        private HoldDisplayInfo(int chargeTicks, int maxHoldTicks, boolean triggerMode, int pageSlot, int rawIndex) {
            this.chargeTicks = chargeTicks;
            this.maxHoldTicks = maxHoldTicks;
            this.triggerMode = triggerMode;
            this.pageSlot = pageSlot;
            this.rawIndex = rawIndex;
        }
    }

    private static Map<String, HoldRuntimeState> getHoldStateStore(World world) {
        return world.isRemote ? CLIENT_HOLD_STATES : SERVER_HOLD_STATES;
    }

    private static String getHoldStateKey(EntityPlayer player, EnumHand hand) {
        return player.getUniqueID() + ":" + hand.name();
    }

    private static HoldRuntimeState getHoldState(EntityPlayer player, EnumHand hand, World world) {
        return getHoldStateStore(world).get(getHoldStateKey(player, hand));
    }

    private static HoldRuntimeState getMainHandHoldState(EntityPlayer player, World world) {
        return getHoldState(player, EnumHand.MAIN_HAND, world);
    }

    public static boolean isClientHoldActive(EntityPlayer player) {
        return player != null && getMainHandHoldState(player, player.world) != null;
    }

    public static int getClientHoldTicks(EntityPlayer player) {
        HoldRuntimeState state = player == null ? null : getMainHandHoldState(player, player.world);
        return state == null ? 0 : state.heldTicks;
    }

    public static void clearClientHoldState(EntityPlayer player) {
        if (player != null) {
            CLIENT_HOLD_STATES.remove(getHoldStateKey(player, EnumHand.MAIN_HAND));
        }
    }

    @Nullable
    public static HoldDisplayInfo getSelectedMainHandHoldDisplayInfo(EntityPlayer player) {
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

    public boolean triggerKeybindSkill(ItemStack bookStack, EntityPlayer player, String keyId,
                                       IKeybindSkillSpell.KeyAction action) {
        if (ToolHelper.isBroken(bookStack) || player == null || keyId == null || keyId.isEmpty()) {
            return false;
        }

        KeybindSkillTarget target = resolveKeybindSkill(bookStack, keyId);
        if (target == null) {
            return false;
        }

        boolean onCooldown = target.page.isRawSpellOnCooldown(
                target.pageStack, target.rawIndex, player.world, player, bookStack);
        SpellContext context = new SpellContext(
                player.world,
                player,
                bookStack,
                target.pageStack,
                target.pageData,
                target.slotType,
                action == IKeybindSkillSpell.KeyAction.PRESS ? TriggerSource.skillPress() : TriggerSource.skillRelease(),
                null
        );

        IKeybindSkillSpell.KeybindResult result = target.spell.onKeybindTriggered(context, action, onCooldown);
        if (!result.isSuccess()) {
            return false;
        }

        target.pageStack.setTagCompound(context.pageData);
        getInventory(bookStack).setStackInSlot(target.pageSlot, target.pageStack);
        if (result.shouldApplyCooldown()) {
            target.page.applyRawSpellCooldown(target.pageStack, target.rawIndex, player.world, player, bookStack);
        }
        if (action == IKeybindSkillSpell.KeyAction.PRESS) {
            ToolHelper.damageTool(bookStack, DURABILITY_COST, player);
        }
        return true;
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

    private boolean isSelectedSpellOnCooldown(ItemStack bookStack, EntityPlayer player, MagicPageItem.SlotType slotType) {
        if (player == null) {
            return false;
        }
        List<SpellEntry> spells = buildSpellList(bookStack, slotType);
        NBTTagCompound tag = TagUtil.getTagSafe(bookStack);
        String key = (slotType == MagicPageItem.SlotType.LEFT) ? TAG_CUR_LEFT_INDEX : TAG_CUR_RIGHT_INDEX;
        int selectedIndex = tag.getInteger(key);
        if (selectedIndex < 0 || selectedIndex >= spells.size()) {
            return false;
        }
        SpellEntry entry = spells.get(selectedIndex);
        if (!(entry.page instanceof UnifiedMagicPage)) {
            return false;
        }
        UnifiedMagicPage page = (UnifiedMagicPage) entry.page;
        long worldTime = player.world.getTotalWorldTime();

        for (UnifiedMagicPage.SpellDisplayData data : page.getAllSpellDisplayData(entry.pageStack)) {
            if (data.internalIndex != entry.internalIndex) continue;
            if (data.cooldownTicks <= 0) return false;
            NBTTagCompound cooldowns = data.pageData.getCompoundTag(TAG_COOLDOWNS);
            long lastUsed = cooldowns.getLong(String.valueOf(entry.internalIndex));
            long cooldownEnd = lastUsed + data.cooldownTicks;
            return worldTime < cooldownEnd;
        }
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {

        if (player.isSneaking() || ToolHelper.isBroken(stack)) {
            return true;
        }

        executeSpell(stack, player, MagicPageItem.SlotType.LEFT, entity);

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

        HoldSpellTarget holdTarget = resolveSelectedHoldSpell(stack, MagicPageItem.SlotType.RIGHT);
        if (holdTarget != null && supportsHold(holdTarget.spell)) {
            if (world.isRemote && isSelectedSpellOnCooldown(stack, player, MagicPageItem.SlotType.RIGHT)) {
                return new ActionResult<>(EnumActionResult.PASS, stack);
            }
            if (!world.isRemote && holdTarget.page.isRawSpellOnCooldown(
                    holdTarget.pageStack, holdTarget.rawIndex, world, player, stack)) {
                return new ActionResult<>(EnumActionResult.PASS, stack);
            }

            String mode = getHoldMode(holdTarget.spell);
            if (!mode.isEmpty()) {
                player.setActiveHand(hand);
                HoldRuntimeState state = getHoldState(player, hand, world);
                boolean sameActiveHold = state != null
                        && state.pageSlot == holdTarget.pageSlot
                        && state.rawIndex == holdTarget.rawIndex
                        && mode.equals(state.mode);
                if (!sameActiveHold) {
                    startHoldState(player, holdTarget, mode, hand, world);
                }
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }

        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        executeSpell(stack, player, MagicPageItem.SlotType.RIGHT, null);
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (living.world.isRemote || !(living instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) living;

        ItemStack active = player.getActiveItemStack();
        boolean stillUsingThisBook = player.isHandActive()
                && player.getActiveHand() == EnumHand.MAIN_HAND
                && !active.isEmpty()
                && active.getItem() == stack.getItem();
        if (!stillUsingThisBook) {
            clearHoldState(player, EnumHand.MAIN_HAND, player.world);
            return;
        }

        HoldRuntimeState state = getMainHandHoldState(player, player.world);
        if (state == null) {
            return;
        }

        HoldSpellTarget target = resolveHoldSpellFromState(stack, state);
        if (target == null) {
            clearHoldState(player, EnumHand.MAIN_HAND, player.world);
            return;
        }

        state.heldTicks = (int) Math.max(0L, player.world.getTotalWorldTime() - state.startWorldTick + 1L);
        NBTTagCompound beforePageData = target.pageData.copy();

        SpellContext context = new SpellContext(
                player.world,
                player,
                stack,
                target.pageStack,
                target.pageData,
                MagicPageItem.SlotType.RIGHT,
                TriggerSource.holdTick(),
                null
        );

        if (HOLD_MODE_CHANNEL.equals(state.mode) && target.spell instanceof IChannelReleaseSpell) {
            ((IChannelReleaseSpell) target.spell).onChannelTick(context, state.heldTicks);
        } else if (HOLD_MODE_TRIGGER.equals(state.mode) && target.spell instanceof IHoldTriggerSpell) {
            IHoldTriggerSpell holdSpell = (IHoldTriggerSpell) target.spell;
            if (state.heldTicks >= holdSpell.getTriggerStartTicks(context)) {
                holdSpell.onHoldTriggerTick(context, state.heldTicks);
            }
            int maxHoldTicks = holdSpell.getMaxHoldTicks(context);
            if (maxHoldTicks > 0 && state.heldTicks >= maxHoldTicks) {
                target.pageStack.setTagCompound(context.pageData);
                if (!context.pageData.equals(beforePageData)) {
                    getInventory(stack).setStackInSlot(target.pageSlot, target.pageStack);
                }
                player.stopActiveHand();
                return;
            }
        }

        target.pageStack.setTagCompound(context.pageData);
        if (!context.pageData.equals(beforePageData)) {
            getInventory(stack).setStackInSlot(target.pageSlot, target.pageStack);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        if (world.isRemote) {
            if (living instanceof EntityPlayer) {
                clearHoldState((EntityPlayer) living, EnumHand.MAIN_HAND, world);
            }
            return;
        }
        if (!(living instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) living;
        HoldRuntimeState state = getMainHandHoldState(player, world);
        if (state == null) {
            return;
        }

        HoldSpellTarget target = resolveHoldSpellFromState(stack, state);
        if (target == null) {
            clearHoldState(player, EnumHand.MAIN_HAND, world);
            return;
        }

        SpellContext releaseContext = new SpellContext(
                world,
                player,
                stack,
                target.pageStack,
                target.pageData,
                MagicPageItem.SlotType.RIGHT,
                TriggerSource.holdRelease(),
                null
        );

        if (HOLD_MODE_CHANNEL.equals(state.mode) && target.spell instanceof IChannelReleaseSpell) {
            IChannelReleaseSpell channelSpell = (IChannelReleaseSpell) target.spell;
            int minTicks = Math.max(0, channelSpell.getMinChannelTicks(releaseContext));
            boolean completed = state.heldTicks >= minTicks;
            if (!completed) {
                channelSpell.onChannelInterrupted(releaseContext, state.heldTicks);
            } else {
                boolean released = channelSpell.onChannelRelease(releaseContext, state.heldTicks, true);
                if (released) {
                    target.page.applyRawSpellCooldown(target.pageStack, target.rawIndex, world, player, stack);
                    ToolHelper.damageTool(stack, DURABILITY_COST, player);
                }
            }
        } else if (HOLD_MODE_TRIGGER.equals(state.mode) && target.spell instanceof IHoldTriggerSpell) {
            IHoldTriggerSpell holdSpell = (IHoldTriggerSpell) target.spell;
            int startTicks = Math.max(0, holdSpell.getTriggerStartTicks(releaseContext));
            boolean interrupted = state.heldTicks < startTicks;
            holdSpell.onHoldEnd(releaseContext, state.heldTicks, interrupted);
            if (!interrupted) {
                target.page.applyRawSpellCooldown(target.pageStack, target.rawIndex, world, player, stack);
            }
        }

        target.pageStack.setTagCompound(releaseContext.pageData);
        getInventory(stack).setStackInSlot(target.pageSlot, target.pageStack);
        clearHoldState(player, EnumHand.MAIN_HAND, world);
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

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (!(entity instanceof EntityPlayer) || !isSelected) return;
        EntityPlayer player = (EntityPlayer) entity;

        if (!world.isRemote) {
            HoldRuntimeState state = getMainHandHoldState(player, world);
            if (state != null) {
                ItemStack active = player.getActiveItemStack();
                boolean stillUsingThisBook = player.isHandActive()
                        && player.getActiveHand() == EnumHand.MAIN_HAND
                        && !active.isEmpty()
                        && active.getItem() == stack.getItem();
                if (!stillUsingThisBook) {
                    clearHoldState(player, EnumHand.MAIN_HAND, world);
                }
            }
            cleanupExcessPages(stack, player);
        } else if (!player.isHandActive()) {
            clearHoldState(player, EnumHand.MAIN_HAND, world);
        }

        BookInventory inv = getInventory(stack);

        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof MagicPageItem)) continue;
            MagicPageItem page = (MagicPageItem) pageStack.getItem();
            MagicPageItem.SlotType slotType = (slot < inv.getLeftSlots()) ? MagicPageItem.SlotType.LEFT : MagicPageItem.SlotType.RIGHT;

            NBTTagCompound oldData = pageStack.getTagCompound();
            if (oldData == null) oldData = new NBTTagCompound();
            NBTTagCompound newData = oldData.copy();

            page.onHeldUpdate(world, player, stack, newData, slotType, pageStack);
            if (!newData.equals(oldData)) {
                pageStack.setTagCompound(newData);
                inv.setStackInSlot(slot, pageStack);
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

    private HoldSpellTarget resolveSelectedHoldSpell(ItemStack stack, MagicPageItem.SlotType slot) {
        validateSpellIndices(stack);
        List<SpellEntry> spells = buildSpellList(stack, slot);
        if (spells.isEmpty()) {
            return null;
        }

        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        String key = slot == MagicPageItem.SlotType.LEFT ? TAG_CUR_LEFT_INDEX : TAG_CUR_RIGHT_INDEX;
        int selectedIndex = tag.getInteger(key);
        if (selectedIndex < 0 || selectedIndex >= spells.size()) {
            return null;
        }

        SpellEntry entry = spells.get(selectedIndex);
        if (!(entry.page instanceof UnifiedMagicPage)) {
            return null;
        }

        UnifiedMagicPage page = (UnifiedMagicPage) entry.page;
        UnifiedMagicPage.SelectedSpell selected = page.resolveSelectedSpell(slot, entry.internalIndex);
        if (selected == null) {
            return null;
        }

        NBTTagCompound pageData = entry.pageStack.getTagCompound();
        if (pageData == null) {
            pageData = new NBTTagCompound();
        }

        return new HoldSpellTarget(page, entry.pageStack, entry.slot, selected.spell, selected.rawIndex, pageData);
    }

    @Nullable
    private KeybindSkillTarget resolveKeybindSkill(ItemStack stack, String keyId) {
        BookInventory inv = getInventory(stack);
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof UnifiedMagicPage)) {
                continue;
            }

            UnifiedMagicPage page = (UnifiedMagicPage) pageStack.getItem();
            MagicPageItem.SlotType slotType = slot < inv.getLeftSlots()
                    ? MagicPageItem.SlotType.LEFT
                    : MagicPageItem.SlotType.RIGHT;

            List<ISpell> spells = page.getRawSpells(slotType);
            for (int rawIndex = 0; rawIndex < spells.size(); rawIndex++) {
                ISpell spell = spells.get(rawIndex);
                if (!(spell instanceof IKeybindSkillSpell)) {
                    continue;
                }

                IKeybindSkillSpell keySpell = (IKeybindSkillSpell) spell;
                if (!keyId.equalsIgnoreCase(keySpell.getKeyBindingId())) {
                    continue;
                }

                NBTTagCompound pageData = pageStack.getTagCompound();
                if (pageData == null) {
                    pageData = new NBTTagCompound();
                }
                return new KeybindSkillTarget(page, pageStack, slot, keySpell, rawIndex, slotType, pageData);
            }
        }
        return null;
    }

    @Nullable
    public HoldDisplayInfo getSelectedHoldDisplayInfo(ItemStack stack, MagicPageItem.SlotType slot, EntityPlayer player) {
        if (player == null) {
            return null;
        }

        HoldSpellTarget target = resolveSelectedHoldSpell(stack, slot);
        if (target == null || !supportsHold(target.spell)) {
            return null;
        }

        NBTTagCompound contextData = target.pageData == null ? new NBTTagCompound() : target.pageData;
        SpellContext context = new SpellContext(
                player.world,
                player,
                stack,
                target.pageStack,
                contextData,
                slot,
                TriggerSource.holdTick(),
                null
        );

        if (target.spell instanceof IHoldTriggerSpell) {
            IHoldTriggerSpell holdSpell = (IHoldTriggerSpell) target.spell;
            int chargeTicks = Math.max(0, holdSpell.getTriggerStartTicks(context));
            int maxHoldTicks = holdSpell.getMaxHoldTicks(context);
            return new HoldDisplayInfo(chargeTicks, maxHoldTicks, true, target.pageSlot, target.rawIndex);
        }

        if (target.spell instanceof IChannelReleaseSpell) {
            IChannelReleaseSpell channelSpell = (IChannelReleaseSpell) target.spell;
            int chargeTicks = Math.max(0, channelSpell.getMinChannelTicks(context));
            return new HoldDisplayInfo(chargeTicks, -1, false, target.pageSlot, target.rawIndex);
        }

        return null;
    }

    private HoldSpellTarget resolveHoldSpellFromState(ItemStack stack, HoldRuntimeState state) {
        if (state == null) {
            return null;
        }

        int pageSlot = state.pageSlot;
        int rawIndex = state.rawIndex;

        BookInventory inv = getInventory(stack);
        if (pageSlot < 0 || pageSlot >= inv.getSlots()) {
            return null;
        }

        ItemStack pageStack = inv.getStackInSlot(pageSlot);
        if (pageStack.isEmpty() || !(pageStack.getItem() instanceof UnifiedMagicPage)) {
            return null;
        }

        UnifiedMagicPage page = (UnifiedMagicPage) pageStack.getItem();
        UnifiedMagicPage.SelectedSpell selected = page.resolveRawSpell(MagicPageItem.SlotType.RIGHT, rawIndex);
        if (selected == null) {
            return null;
        }

        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) {
            pageData = new NBTTagCompound();
        }

        return new HoldSpellTarget(page, pageStack, pageSlot, selected.spell, selected.rawIndex, pageData);
    }

    private static boolean supportsHold(ISpell spell) {
        return spell instanceof IChannelReleaseSpell || spell instanceof IHoldTriggerSpell;
    }

    private static String getHoldMode(ISpell spell) {
        if (spell instanceof IChannelReleaseSpell) {
            return HOLD_MODE_CHANNEL;
        }
        if (spell instanceof IHoldTriggerSpell) {
            return HOLD_MODE_TRIGGER;
        }
        return "";
    }

    private void startHoldState(EntityPlayer player, HoldSpellTarget target, String mode, EnumHand hand, World world) {
        HoldRuntimeState state = new HoldRuntimeState(target.pageSlot, target.rawIndex, mode, hand, world.getTotalWorldTime());
        getHoldStateStore(world).put(getHoldStateKey(player, hand), state);
    }

    private void clearHoldState(EntityPlayer player, EnumHand hand, World world) {
        getHoldStateStore(world).remove(getHoldStateKey(player, hand));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    // Keep bow pull/stretch animation while holding, but avoid twitch from NBT updates.
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && !oldStack.isEmpty() && !newStack.isEmpty()
                && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
