package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.tools.magicbook.gui.BookInventory;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.page.UnifiedMagicPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IChannelReleaseSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IHoldTriggerSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindSkillSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MagicBookStateHelper {

    public enum HoldCastModel {
        CHANNEL_RELEASE,
        HOLD_TRIGGER
    }

    public static final class SpellEntry {
        public final ItemStack pageStack;
        public final UnifiedMagicPage page;
        public final int pageSlot;
        public final int internalIndex;

        private SpellEntry(ItemStack pageStack, UnifiedMagicPage page, int pageSlot, int internalIndex) {
            this.pageStack = pageStack;
            this.page = page;
            this.pageSlot = pageSlot;
            this.internalIndex = internalIndex;
        }
    }

    public static final class ResolvedSpellTarget {
        public final UnifiedMagicPage page;
        public final ItemStack pageStack;
        public final int bookmarkId;
        public final ISpell spell;
        public final int rawIndex;
        public final MagicPageItem.SlotType slotType;
        public final NBTTagCompound pageData;

        private ResolvedSpellTarget(UnifiedMagicPage page, ItemStack pageStack, int bookmarkId,
                                    ISpell spell, int rawIndex, MagicPageItem.SlotType slotType,
                                    NBTTagCompound pageData) {
            this.page = page;
            this.pageStack = pageStack;
            this.bookmarkId = bookmarkId;
            this.spell = spell;
            this.rawIndex = rawIndex;
            this.slotType = slotType;
            this.pageData = pageData;
        }
    }

    public static final class HoldRuntimeState {
        public final int bookmarkId;
        public final int rawIndex;
        public final HoldCastModel model;
        public final EnumHand hand;
        public final long startWorldTick;
        public int heldTicks;
        public boolean activationEvaluated;
        public boolean activationApproved;
        public boolean hasProducedEffect;

        private HoldRuntimeState(int bookmarkId, int rawIndex, HoldCastModel model,
                                 EnumHand hand, long startWorldTick) {
            this.bookmarkId = bookmarkId;
            this.rawIndex = rawIndex;
            this.model = model;
            this.hand = hand;
            this.startWorldTick = startWorldTick;
        }
    }

    public static final class HoldDisplayInfo {
        public final int chargeTicks;
        public final int maxHoldTicks;
        public final boolean triggerMode;
        public final int bookmarkId;
        public final int rawIndex;

        private HoldDisplayInfo(int chargeTicks, int maxHoldTicks, boolean triggerMode,
                                int bookmarkId, int rawIndex) {
            this.chargeTicks = chargeTicks;
            this.maxHoldTicks = maxHoldTicks;
            this.triggerMode = triggerMode;
            this.bookmarkId = bookmarkId;
            this.rawIndex = rawIndex;
        }
    }

    private static final Map<String, HoldRuntimeState> SERVER_HOLD_STATES = new ConcurrentHashMap<>();
    private static final Map<String, HoldRuntimeState> CLIENT_HOLD_STATES = new ConcurrentHashMap<>();

    private final MagicBook book;
    private final Map<Integer, WeakReference<BookInventory>> inventoryCache = new ConcurrentHashMap<>();

    public MagicBookStateHelper(MagicBook book) {
        this.book = book;
    }

    public BookInventory getInventory(ItemStack stack) {
        int key = System.identityHashCode(stack);
        WeakReference<BookInventory> ref = inventoryCache.get(key);
        BookInventory inv = ref == null ? null : ref.get();
        if (inv != null && inv.getBookStack() == stack) {
            return inv;
        }

        int[] slotCounts = getSlotCounts(stack);
        inv = new BookInventory(stack, slotCounts[0], slotCounts[1]);
        inventoryCache.put(key, new WeakReference<>(inv));
        inventoryCache.entrySet().removeIf(entry -> entry.getValue().get() == null);
        return inv;
    }

    public List<SpellEntry> buildSpellList(ItemStack stack, MagicPageItem.SlotType slotType) {
        List<SpellEntry> list = new ArrayList<>();
        BookInventory inv = getInventory(stack);
        int start = slotType == MagicPageItem.SlotType.LEFT ? 0 : inv.getLeftSlots();
        int end = slotType == MagicPageItem.SlotType.LEFT ? inv.getLeftSlots() : inv.getSlots();

        for (int slot = start; slot < end; slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof UnifiedMagicPage)) {
                continue;
            }
            UnifiedMagicPage page = (UnifiedMagicPage) pageStack.getItem();
            int spellCount = page.getSpellCount(slotType);
            for (int internalIndex = 0; internalIndex < spellCount; internalIndex++) {
                list.add(new SpellEntry(pageStack, page, slot, internalIndex));
            }
        }
        return list;
    }

    public void validateSpellIndices(ItemStack stack) {
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        boolean dirty = false;

        List<SpellEntry> leftSpells = buildSpellList(stack, MagicPageItem.SlotType.LEFT);
        int curLeft = tag.getInteger(MagicBookKeys.TAG_CUR_LEFT_INDEX);
        if (curLeft < 0 || (leftSpells.isEmpty() && curLeft != 0) || (!leftSpells.isEmpty() && curLeft >= leftSpells.size())) {
            tag.setInteger(MagicBookKeys.TAG_CUR_LEFT_INDEX, 0);
            dirty = true;
        }

        List<SpellEntry> rightSpells = buildSpellList(stack, MagicPageItem.SlotType.RIGHT);
        int curRight = tag.getInteger(MagicBookKeys.TAG_CUR_RIGHT_INDEX);
        if (curRight < 0 || (rightSpells.isEmpty() && curRight != 0) || (!rightSpells.isEmpty() && curRight >= rightSpells.size())) {
            tag.setInteger(MagicBookKeys.TAG_CUR_RIGHT_INDEX, 0);
            dirty = true;
        }

        if (dirty) {
            stack.setTagCompound(tag);
        }
    }

    @Nullable
    public ResolvedSpellTarget resolveSelectedSpellTarget(ItemStack bookStack, MagicPageItem.SlotType slotType) {
        validateSpellIndices(bookStack);
        List<SpellEntry> spells = buildSpellList(bookStack, slotType);
        if (spells.isEmpty()) {
            return null;
        }

        NBTTagCompound tag = TagUtil.getTagSafe(bookStack);
        String key = slotType == MagicPageItem.SlotType.LEFT
                ? MagicBookKeys.TAG_CUR_LEFT_INDEX
                : MagicBookKeys.TAG_CUR_RIGHT_INDEX;
        int selectedIndex = tag.getInteger(key);
        if (selectedIndex < 0 || selectedIndex >= spells.size()) {
            return null;
        }

        SpellEntry entry = spells.get(selectedIndex);
        NBTTagCompound pageData = entry.pageStack.getTagCompound();
        if (pageData == null) {
            pageData = new NBTTagCompound();
        }
        pageData.setInteger(MagicBookKeys.TAG_SPELL_INDEX, entry.internalIndex);

        UnifiedMagicPage.SelectedSpell selected = entry.page.resolveSelectedSpell(slotType, entry.internalIndex);
        if (selected == null) {
            return null;
        }

        return new ResolvedSpellTarget(
                entry.page,
                entry.pageStack,
                entry.pageSlot,
                selected.spell,
                selected.rawIndex,
                slotType,
                pageData
        );
    }

    @Nullable
    public ResolvedSpellTarget resolveSelectedHoldSpell(ItemStack stack, MagicPageItem.SlotType slotType) {
        ResolvedSpellTarget target = resolveSelectedSpellTarget(stack, slotType);
        if (target == null || !supportsHold(target.spell)) {
            return null;
        }
        return target;
    }

    @Nullable
    public ResolvedSpellTarget resolveKeybindSpell(ItemStack stack, String keyId) {
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
                return new ResolvedSpellTarget(page, pageStack, slot, keySpell, rawIndex, slotType, pageData);
            }
        }
        return null;
    }

    @Nullable
    public ResolvedSpellTarget resolveHoldSpellFromState(ItemStack stack, HoldRuntimeState state) {
        if (state == null) {
            return null;
        }

        BookInventory inv = getInventory(stack);
        if (state.bookmarkId < 0 || state.bookmarkId >= inv.getSlots()) {
            return null;
        }

        ItemStack pageStack = inv.getStackInSlot(state.bookmarkId);
        if (pageStack.isEmpty() || !(pageStack.getItem() instanceof UnifiedMagicPage)) {
            return null;
        }

        UnifiedMagicPage page = (UnifiedMagicPage) pageStack.getItem();
        UnifiedMagicPage.SelectedSpell selected = page.resolveRawSpell(MagicPageItem.SlotType.RIGHT, state.rawIndex);
        if (selected == null) {
            return null;
        }

        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) {
            pageData = new NBTTagCompound();
        }

        return new ResolvedSpellTarget(
                page,
                pageStack,
                state.bookmarkId,
                selected.spell,
                selected.rawIndex,
                MagicPageItem.SlotType.RIGHT,
                pageData
        );
    }

    public boolean hasSpell(ItemStack bookStack, String spellRegistryName) {
        if (ToolHelper.isBroken(bookStack)
                || spellRegistryName == null
                || spellRegistryName.trim().isEmpty()) {
            return false;
        }
        String normalizedName = spellRegistryName.trim();

        BookInventory inv = getInventory(bookStack);
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof UnifiedMagicPage)) {
                continue;
            }

            UnifiedMagicPage page = (UnifiedMagicPage) pageStack.getItem();
            MagicPageItem.SlotType slotType = slot < inv.getLeftSlots()
                    ? MagicPageItem.SlotType.LEFT
                    : MagicPageItem.SlotType.RIGHT;
            for (ISpell spell : page.getRawSpells(slotType)) {
                if (normalizedName.equalsIgnoreCase(spell.getNameKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void switchSpell(ItemStack stack, MagicPageItem.SlotType slotType, boolean next) {
        validateSpellIndices(stack);
        NBTTagCompound tag = TagUtil.getTagSafe(stack);
        String key = slotType == MagicPageItem.SlotType.LEFT
                ? MagicBookKeys.TAG_CUR_LEFT_INDEX
                : MagicBookKeys.TAG_CUR_RIGHT_INDEX;
        List<SpellEntry> spells = buildSpellList(stack, slotType);
        if (spells.isEmpty()) {
            tag.setInteger(key, 0);
        } else {
            int current = tag.getInteger(key);
            if (current < 0 || current >= spells.size()) {
                current = 0;
            }
            current = next ? (current + 1) % spells.size() : (current - 1 + spells.size()) % spells.size();
            tag.setInteger(key, current);
        }
        stack.setTagCompound(tag);
    }

    public boolean isSelectedSpellOnCooldown(ItemStack bookStack, EntityPlayer player, MagicPageItem.SlotType slotType) {
        if (player == null) {
            return false;
        }

        List<SpellEntry> spells = buildSpellList(bookStack, slotType);
        NBTTagCompound tag = TagUtil.getTagSafe(bookStack);
        String key = slotType == MagicPageItem.SlotType.LEFT
                ? MagicBookKeys.TAG_CUR_LEFT_INDEX
                : MagicBookKeys.TAG_CUR_RIGHT_INDEX;
        int selectedIndex = tag.getInteger(key);
        if (selectedIndex < 0 || selectedIndex >= spells.size()) {
            return false;
        }

        SpellEntry entry = spells.get(selectedIndex);
        long worldTime = player.world.getTotalWorldTime();
        for (UnifiedMagicPage.SpellDisplayData data : entry.page.getAllSpellDisplayData(entry.pageStack)) {
            if (data.internalIndex != entry.internalIndex || data.cooldownTicks <= 0) {
                continue;
            }
            NBTTagCompound cooldowns = data.pageData.getCompoundTag(MagicBookKeys.TAG_COOLDOWNS);
            long lastUsed = cooldowns.getLong(String.valueOf(entry.internalIndex));
            return worldTime < lastUsed + data.cooldownTicks;
        }
        return false;
    }

    @Nullable
    public HoldDisplayInfo getSelectedHoldDisplayInfo(ItemStack stack, MagicPageItem.SlotType slotType, EntityPlayer player) {
        if (player == null) {
            return null;
        }

        ResolvedSpellTarget target = resolveSelectedHoldSpell(stack, slotType);
        if (target == null) {
            return null;
        }

        NBTTagCompound contextData = target.pageData == null ? new NBTTagCompound() : target.pageData;
        SpellContext context = new SpellContext(
                player.world,
                player,
                stack,
                target.pageStack,
                contextData,
                slotType,
                TriggerSource.holdTick(),
                null
        );

        if (target.spell instanceof IHoldTriggerSpell) {
            IHoldTriggerSpell holdSpell = (IHoldTriggerSpell) target.spell;
            return new HoldDisplayInfo(
                    Math.max(0, holdSpell.getTriggerStartTicks(context)),
                    holdSpell.getMaxHoldTicks(context),
                    true,
                    target.bookmarkId,
                    target.rawIndex
            );
        }

        if (target.spell instanceof IChannelReleaseSpell) {
            IChannelReleaseSpell channelSpell = (IChannelReleaseSpell) target.spell;
            return new HoldDisplayInfo(
                    Math.max(0, channelSpell.getMinChannelTicks(context)),
                    -1,
                    false,
                    target.bookmarkId,
                    target.rawIndex
            );
        }

        return null;
    }

    public void onSelectedUpdate(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            HoldRuntimeState state = getMainHandHoldState(player, world);
            if (state != null && !isStillUsingThisBook(player, stack, EnumHand.MAIN_HAND)) {
                clearHoldState(player, EnumHand.MAIN_HAND, world);
            }
            cleanupExcessPages(stack, player);
        } else if (!player.isHandActive()) {
            clearHoldState(player, EnumHand.MAIN_HAND, world);
        }

        BookInventory inv = getInventory(stack);
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof MagicPageItem)) {
                continue;
            }
            MagicPageItem page = (MagicPageItem) pageStack.getItem();
            MagicPageItem.SlotType slotType = slot < inv.getLeftSlots()
                    ? MagicPageItem.SlotType.LEFT
                    : MagicPageItem.SlotType.RIGHT;

            NBTTagCompound oldData = pageStack.getTagCompound();
            if (oldData == null) {
                oldData = new NBTTagCompound();
            }
            NBTTagCompound newData = oldData.copy();
            page.onHeldUpdate(world, player, stack, newData, slotType, pageStack);
            if (!newData.equals(oldData)) {
                pageStack.setTagCompound(newData);
                inv.setStackInSlot(slot, pageStack);
            }
        }
    }

    public void savePageData(ItemStack bookStack, ResolvedSpellTarget target) {
        target.pageStack.setTagCompound(target.pageData);
        getInventory(bookStack).setStackInSlot(target.bookmarkId, target.pageStack);
    }

    public int[] getSlotCounts(ItemStack stack) {
        BookPageStats stats = getCoreBookPageStats(stack);
        int left = stats != null ? stats.leftSlots : 1;
        int right = stats != null ? stats.rightSlots : 1;
        return new int[]{left, right};
    }

    public HoldRuntimeState getMainHandHoldState(EntityPlayer player, World world) {
        return getHoldState(player, EnumHand.MAIN_HAND, world);
    }

    @Nullable
    public HoldRuntimeState getHoldState(EntityPlayer player, EnumHand hand, World world) {
        return getHoldStateStore(world).get(getHoldStateKey(player, hand));
    }

    public void startHoldState(EntityPlayer player, ResolvedSpellTarget target, HoldCastModel model,
                               EnumHand hand, World world) {
        HoldRuntimeState state = new HoldRuntimeState(target.bookmarkId, target.rawIndex, model, hand, world.getTotalWorldTime());
        getHoldStateStore(world).put(getHoldStateKey(player, hand), state);
    }

    public void clearHoldState(EntityPlayer player, EnumHand hand, World world) {
        getHoldStateStore(world).remove(getHoldStateKey(player, hand));
    }

    public boolean isSameHoldState(HoldRuntimeState state, ResolvedSpellTarget target, HoldCastModel model) {
        return state != null
                && state.bookmarkId == target.bookmarkId
                && state.rawIndex == target.rawIndex
                && state.model == model;
    }

    public boolean isStillUsingThisBook(EntityPlayer player, ItemStack stack, EnumHand hand) {
        ItemStack active = player.getActiveItemStack();
        return player.isHandActive()
                && player.getActiveHand() == hand
                && !active.isEmpty()
                && active.getItem() == stack.getItem();
    }

    @Nullable
    public HoldCastModel getHoldCastModel(ISpell spell) {
        if (spell instanceof IChannelReleaseSpell) {
            return HoldCastModel.CHANNEL_RELEASE;
        }
        if (spell instanceof IHoldTriggerSpell) {
            return HoldCastModel.HOLD_TRIGGER;
        }
        return null;
    }

    public boolean supportsHold(ISpell spell) {
        return getHoldCastModel(spell) != null;
    }

    public static boolean isClientHoldActive(EntityPlayer player) {
        return player != null && getMainHandHoldStateStatic(player, player.world) != null;
    }

    public static int getClientHoldTicks(EntityPlayer player) {
        HoldRuntimeState state = player == null ? null : getMainHandHoldStateStatic(player, player.world);
        return state == null ? 0 : state.heldTicks;
    }

    public static void clearClientHoldState(EntityPlayer player) {
        if (player != null) {
            CLIENT_HOLD_STATES.remove(getHoldStateKey(player, EnumHand.MAIN_HAND));
        }
    }

    @Nullable
    private BookPageStats getCoreBookPageStats(ItemStack toolStack) {
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(toolStack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() < 3) {
            return null;
        }
        Material pageMat = materials.get(2);
        return pageMat.getStats(BookPageStats.TYPE);
    }

    private void cleanupExcessPages(ItemStack stack, @Nullable EntityPlayer player) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(MagicBookKeys.TAG_BOOK_INVENTORY, 10)) {
            return;
        }

        int[] slotCounts = getSlotCounts(stack);
        int leftSlots = slotCounts[0];
        int rightSlots = slotCounts[1];

        int lastLeft = tag.getInteger(MagicBookKeys.TAG_LAST_LEFT_SLOTS);
        int lastRight = tag.getInteger(MagicBookKeys.TAG_LAST_RIGHT_SLOTS);
        if (lastLeft == leftSlots && lastRight == rightSlots) {
            return;
        }

        NBTTagCompound invTag = tag.getCompoundTag(MagicBookKeys.TAG_BOOK_INVENTORY);
        List<ItemStack> excess = new ArrayList<>();

        if (invTag.hasKey("Left", 10)) {
            ItemStack[] kept = new ItemStack[leftSlots];
            BookInventory temp = new BookInventory(stack, leftSlots, rightSlots);
            temp.deserializeNBT(invTag);
            for (int i = 0; i < temp.getLeftSlots(); i++) {
                if (i < leftSlots) {
                    kept[i] = temp.getStackInSlot(i);
                } else if (!temp.getStackInSlot(i).isEmpty()) {
                    excess.add(temp.getStackInSlot(i));
                }
            }
        }

        if (invTag.hasKey("Right", 10)) {
            BookInventory temp = new BookInventory(stack, leftSlots, rightSlots);
            temp.deserializeNBT(invTag);
            for (int i = temp.getLeftSlots(); i < temp.getSlots(); i++) {
                int rightIndex = i - temp.getLeftSlots();
                if (rightIndex >= rightSlots) {
                    ItemStack overflow = temp.getStackInSlot(i);
                    if (!overflow.isEmpty()) {
                        excess.add(overflow);
                    }
                }
            }
        }

        BookInventory resized = new BookInventory(stack, leftSlots, rightSlots);
        resized.deserializeNBT(invTag);
        tag.setInteger(MagicBookKeys.TAG_LAST_LEFT_SLOTS, leftSlots);
        tag.setInteger(MagicBookKeys.TAG_LAST_RIGHT_SLOTS, rightSlots);
        tag.setTag(MagicBookKeys.TAG_BOOK_INVENTORY, resized.serializeNBT());
        stack.setTagCompound(tag);

        if (!excess.isEmpty() && player != null && !player.world.isRemote) {
            for (ItemStack extra : excess) {
                player.dropItem(extra, false, true);
            }
        }
    }

    private static Map<String, HoldRuntimeState> getHoldStateStore(World world) {
        return world.isRemote ? CLIENT_HOLD_STATES : SERVER_HOLD_STATES;
    }

    private static String getHoldStateKey(EntityPlayer player, EnumHand hand) {
        return player.getUniqueID() + ":" + hand.name();
    }

    @Nullable
    private static HoldRuntimeState getMainHandHoldStateStatic(EntityPlayer player, World world) {
        return getHoldStateStore(world).get(getHoldStateKey(player, EnumHand.MAIN_HAND));
    }
}
