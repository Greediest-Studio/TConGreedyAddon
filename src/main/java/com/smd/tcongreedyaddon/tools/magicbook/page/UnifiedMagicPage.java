package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellRegistry;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.*;

public class UnifiedMagicPage extends MagicPageItem {

    private static class SpellInfo {
        final ISpell spell;
        final int rawIndex;
        SpellInfo(ISpell spell, int rawIndex) {
            this.spell = spell;
            this.rawIndex = rawIndex;
        }
    }

    public static class SelectedSpell {
        public final ISpell spell;
        public final int rawIndex;

        public SelectedSpell(ISpell spell, int rawIndex) {
            this.spell = spell;
            this.rawIndex = rawIndex;
        }
    }

    private final SlotType slotType;
    private final List<ISpell> leftSpells;
    private final List<ISpell> rightSpells;
    private final List<ISpell> leftSelectable;
    private final List<ISpell> rightSelectable;
    // 事件映射：事件类 -> 法术信息列表
    private final Map<Class<? extends Event>, List<SpellInfo>> eventSpellMap = new HashMap<>();
    private String displayNameKey = "unified.page.default";

    protected UnifiedMagicPage(Builder builder) {
        this.slotType = builder.slotType;

        this.leftSpells = Collections.unmodifiableList(new ArrayList<>(builder.leftSpells));
        this.rightSpells = Collections.unmodifiableList(new ArrayList<>(builder.rightSpells));

        List<ISpell> leftSel = new ArrayList<>();
        for (ISpell s : leftSpells) {
            if (s.isSelectable()) leftSel.add(s);
        }
        this.leftSelectable = Collections.unmodifiableList(leftSel);

        List<ISpell> rightSel = new ArrayList<>();
        for (ISpell s : rightSpells) {
            if (s.isSelectable()) rightSel.add(s);
        }
        this.rightSelectable = Collections.unmodifiableList(rightSel);

        if (builder.displayNameKey != null) this.displayNameKey = builder.displayNameKey;

        buildEventSpellMap(leftSpells);
        buildEventSpellMap(rightSpells);

        setMaxStackSize(1);
        setCreativeTab(TinkerRegistry.tabParts);
    }

    private void buildEventSpellMap(List<ISpell> spells) {
        for (int i = 0; i < spells.size(); i++) {
            ISpell spell = spells.get(i);
            List<Class<? extends Event>> events = spell.getListeningEvents();
            for (Class<? extends Event> eventClass : events) {
                eventSpellMap.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(new SpellInfo(spell, i));
            }
        }
    }

    @Override
    public SlotType getSlotType() {
        return slotType;
    }

    // ==================== 索引管理 ====================

    /**
     * 获取指定槽位中可切换的法术列表
     */
    private List<ISpell> getSelectableSpells(SlotType slot) {
        return slot == SlotType.LEFT ? leftSelectable : rightSelectable;
    }

    private List<ISpell> getAllSpells(SlotType slot) {
        return slot == SlotType.LEFT ? leftSpells : rightSpells;
    }

    public List<ISpell> getRawSpells(SlotType slot) {
        return getAllSpells(slot);
    }

    public SelectedSpell resolveSelectedSpell(SlotType slot, int selectableIndex) {
        List<ISpell> all = getAllSpells(slot);
        int selectableCounter = 0;
        for (int rawIndex = 0; rawIndex < all.size(); rawIndex++) {
            ISpell spell = all.get(rawIndex);
            if (!spell.isSelectable()) {
                continue;
            }
            if (selectableCounter == selectableIndex) {
                return new SelectedSpell(spell, rawIndex);
            }
            selectableCounter++;
        }
        return null;
    }

    public SelectedSpell resolveRawSpell(SlotType slot, int rawIndex) {
        List<ISpell> all = getAllSpells(slot);
        if (rawIndex < 0 || rawIndex >= all.size()) {
            return null;
        }
        return new SelectedSpell(all.get(rawIndex), rawIndex);
    }

    public SelectedSpell resolveSelectedSpellFromData(SlotType slot, NBTTagCompound pageData) {
        if (pageData == null) {
            return null;
        }
        return resolveSelectedSpell(slot, pageData.getInteger("spellIndex"));
    }

    public boolean isRawSpellOnCooldown(ItemStack pageStack, int rawIndex, World world, EntityPlayer player, ItemStack bookStack) {
        SelectedSpell selected = resolveRawSpell(getSlotType(), rawIndex);
        if (selected == null) {
            return false;
        }
        return isSpellOnCooldownRaw(pageStack, rawIndex, world, player, bookStack, selected.spell);
    }

    public void applyRawSpellCooldown(ItemStack pageStack, int rawIndex, World world, EntityPlayer player, ItemStack bookStack) {
        SelectedSpell selected = resolveRawSpell(getSlotType(), rawIndex);
        if (selected == null) {
            return;
        }
        setSpellCooldownRaw(pageStack, rawIndex, world, player, bookStack, selected.spell);
    }

    private boolean isSpellOnCooldownRaw(ItemStack pageStack, int rawIndex, World world, EntityPlayer player, ItemStack bookStack, ISpell spell) {
        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) return false;
        NBTTagCompound cooldowns = pageData.getCompoundTag(TAG_COOLDOWNS);
        long lastUsed = cooldowns.getLong(String.valueOf(rawIndex));
        int cooldown = spell.getCooldownTicks(player, bookStack); // 传入 bookStack
        if (cooldown <= 0) return false;
        long now = world.getTotalWorldTime();
        return now - lastUsed < cooldown;
    }

    private void setSpellCooldownRaw(ItemStack pageStack, int rawIndex, World world, EntityPlayer player, ItemStack bookStack, ISpell spell) {
        int cooldown = spell.getCooldownTicks(player, bookStack);
        if (cooldown <= 0) return;
        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) pageData = new NBTTagCompound();
        NBTTagCompound cooldowns = pageData.getCompoundTag(TAG_COOLDOWNS);
        cooldowns.setLong(String.valueOf(rawIndex), world.getTotalWorldTime());
        pageData.setTag(TAG_COOLDOWNS, cooldowns);
        pageStack.setTagCompound(pageData);
    }

    private boolean executeSpellWithRawIndex(ISpell spell, int rawIndex, SpellContext context, ItemStack pageStack) {
        if (isSpellOnCooldownRaw(pageStack, rawIndex, context.world, context.player, context.bookStack, spell)) return false;
        if (!spell.canTrigger(context)) return false;
        boolean success = spell.execute(context);
        if (success) {
            setSpellCooldownRaw(pageStack, rawIndex, context.world, context.player, context.bookStack, spell);
        }
        return success;
    }

    public boolean executeRawSpell(int rawIndex, SpellContext context) {
        SelectedSpell selected = resolveRawSpell(context.slot, rawIndex);
        if (selected == null) {
            return false;
        }
        return executeSpellWithRawIndex(selected.spell, rawIndex, context, context.pageStack);
    }

    public boolean executeSelectedSpell(SpellContext context) {
        SelectedSpell selected = resolveSelectedSpellFromData(context.slot, context.pageData);
        if (selected == null) {
            return false;
        }
        return executeSpellWithRawIndex(selected.spell, selected.rawIndex, context, context.pageStack);
    }

    // ==================== 实现父类抽象方法 ====================

    @Override
    public int getSpellCount(SlotType slotType) {
        return getSelectableSpells(slotType).size();
    }

    @Override
    public String getSpellDisplayName(int internalIndex, SlotType slotType) {
        List<ISpell> selectable = getSelectableSpells(slotType);
        if (internalIndex < 0 || internalIndex >= selectable.size()) return "Unknown";
        return I18n.translateToLocal(selectable.get(internalIndex).getNameKey());
    }

    @Override
    public int getSpellCooldownTicks(int internalIndex, SlotType slotType) {
        List<ISpell> selectable = getSelectableSpells(slotType);
        if (internalIndex < 0 || internalIndex >= selectable.size()) return 0;
        return selectable.get(internalIndex).getCooldownTicks();
    }

    @Override
    public int getSpellCooldownTicks(int spellIndex) {
        return getSpellCooldownTicks(spellIndex, getSlotType());
    }

    // ==================== 主动施法===================

    @Override
    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target, NBTTagCompound pageData, ItemStack pageStack) {
        if (player.world.isRemote) return false;
        SpellContext context = new SpellContext(player.world, player, toolStack, pageStack, pageData, SlotType.LEFT, TriggerSource.leftClick(), target);
        boolean success = executeSelectedSpell(context);
        if (success) {
            pageStack.setTagCompound(pageData);
        }
        return success;
    }

    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, ItemStack pageStack) {
        if (world.isRemote) return false;
        SpellContext context = new SpellContext(world, player, toolStack, pageStack, pageData, SlotType.RIGHT, TriggerSource.rightClick(), null);
        boolean success = executeSelectedSpell(context);
        if (success) {
            pageStack.setTagCompound(pageData);
        }
        return success;
    }

    // ==================== 被动更新 ====================

    @Override
    public void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, SlotType slot, ItemStack pageStack) {
        List<ISpell> allSpells = slot == SlotType.LEFT ? leftSpells : rightSpells;
        for (int rawIndex = 0; rawIndex < allSpells.size(); rawIndex++) {
            ISpell spell = allSpells.get(rawIndex);
            SpellContext context = new SpellContext(world, player, toolStack, pageStack, pageData, slot, TriggerSource.tick(), null);
            executeSpellWithRawIndex(spell, rawIndex, context, pageStack);
        }
        pageStack.setTagCompound(pageData);
    }

    // ==================== 事件触发 ====================

    public void onEvent(Event event, EntityPlayer player, ItemStack bookStack, ItemStack pageStack, NBTTagCompound pageData, SlotType slot) {
        List<SpellInfo> spellInfos = eventSpellMap.get(event.getClass());
        if (spellInfos == null) return;

        TriggerSource source = TriggerSource.event(event);
        for (SpellInfo info : spellInfos) {
            if (pageData == null) pageData = new NBTTagCompound();
            SpellContext context = new SpellContext(player.world, player, bookStack, pageStack, pageData, slot, source, null);

            info.spell.onEvent(event, context, info.rawIndex);

            if (info.spell.canTrigger(context)) {
                boolean success = executeSpellWithRawIndex(info.spell, info.rawIndex, context, pageStack);
                if (success) {
                    pageData = pageStack.getTagCompound();
                }
            }
        }
        if (pageData != null) {
            pageStack.setTagCompound(pageData);
        }
    }

    @Override
    public int getInitialSpellIndex(ItemStack pageStack) {
        return 0;
    }

    @Override
    public String getCurrentSpellDisplayName(NBTTagCompound pageData, ItemStack pageStack) {
        SlotType slot = getSlotType();
        List<ISpell> selectable = getSelectableSpells(slot);
        if (selectable.isEmpty()) return I18n.translateToLocal(displayNameKey);
        int index = pageData.getInteger("spellIndex");
        if (index >= 0 && index < selectable.size()) {
            return I18n.translateToLocal(selectable.get(index).getNameKey());
        }
        return I18n.translateToLocal(displayNameKey);
    }

    @Override
    public List<String> getAllSpellNames(NBTTagCompound pageData, ItemStack pageStack) {
        SlotType slot = getSlotType();
        List<String> names = new ArrayList<>();
        for (ISpell spell : getSelectableSpells(slot)) {
            names.add(I18n.translateToLocal(spell.getNameKey()));
        }
        return names;
    }

    // ==================== 工具提示 ====================

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (!leftSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.left_spells") + ":");
            for (ISpell spell : leftSelectable) {
                tooltip.add(" - " + I18n.translateToLocal(spell.getNameKey()));
            }
        }

        if (!rightSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.right_spells") + ":");
            for (ISpell spell : rightSelectable) {
                tooltip.add(" - " + I18n.translateToLocal(spell.getNameKey()));
            }
        }

        List<ISpell> leftNonSelectable = new ArrayList<>();
        for (ISpell spell : leftSpells) {
            if (!spell.isSelectable()) leftNonSelectable.add(spell);
        }
        if (!leftNonSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.left_passive") + ":");
            for (ISpell s : leftNonSelectable) {
                tooltip.add(" - " + I18n.translateToLocal(s.getNameKey()));
            }
        }

        List<ISpell> rightNonSelectable = new ArrayList<>();
        for (ISpell spell : rightSpells) {
            if (!spell.isSelectable()) rightNonSelectable.add(spell);
        }
        if (!rightNonSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.right_passive") + ":");
            for (ISpell s : rightNonSelectable) {
                tooltip.add(" - " + I18n.translateToLocal(s.getNameKey()));
            }
        }
    }

    public List<ResourceLocation> getSpellIcons(NBTTagCompound pageData, ItemStack pageStack) {
        SlotType slot = getSlotType();
        List<ResourceLocation> icons = new ArrayList<>();
        List<ISpell> spells = getSelectableSpells(slot);
        for (int i = 0; i < spells.size(); i++) {
            icons.add(spells.get(i).getDisplayIcon(pageData, i));
        }
        return icons;
    }

    public static class Builder {
        private final SlotType slotType;
        private final List<ISpell> leftSpells = new ArrayList<>();
        private final List<ISpell> rightSpells = new ArrayList<>();
        private String displayNameKey;

        public Builder(SlotType slotType) {
            this.slotType = slotType;
        }

        public Builder addLeftSpell(ISpell spell) {
            leftSpells.add(spell);
            return this;
        }

        public Builder addRightSpell(ISpell spell) {
            rightSpells.add(spell);
            return this;
        }

        public Builder displayName(String key) {
            this.displayNameKey = key;
            return this;
        }

        public UnifiedMagicPage build() {
            if (slotType == SlotType.LEFT && !rightSpells.isEmpty()) {
                throw new IllegalStateException("Left page cannot have right spells");
            }
            if (slotType == SlotType.RIGHT && !leftSpells.isEmpty()) {
                throw new IllegalStateException("Right page cannot have left spells");
            }
            UnifiedMagicPage page = new UnifiedMagicPage(this);
            SpellRegistry.registerPage(page, new ArrayList<>(leftSpells), new ArrayList<>(rightSpells));
            return page;
        }
    }

    // ==================== HUD 显示数据 ====================

    public List<SpellDisplayData> getAllSpellDisplayData(ItemStack pageStack) {
        List<SpellDisplayData> list = new ArrayList<>();
        List<ISpell> spells = (slotType == SlotType.LEFT) ? leftSpells : rightSpells;
        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) pageData = new NBTTagCompound();

        for (int i = 0; i < spells.size(); i++) {
            ISpell spell = spells.get(i);
            list.add(new SpellDisplayData(
                    I18n.translateToLocal(spell.getNameKey()),
                    spell.getDisplayIcon(pageData, i),
                    spell.isSelectable(),
                    spell.shouldRenderInOverlay(),
                    i, // 使用原始索引
                    spell.getCooldownTicks(),
                    pageData
            ));
        }
        return list;
    }

    public static class SpellDisplayData {
        public final String name;
        public final ResourceLocation icon;
        public final boolean selectable;
        public final boolean renderInOverlay;
        public final int internalIndex; // 原始索引
        public final int cooldownTicks;
        public final NBTTagCompound pageData;

        public SpellDisplayData(String name, ResourceLocation icon, boolean selectable,
                                boolean renderInOverlay, int internalIndex,
                                int cooldownTicks, NBTTagCompound pageData) {
            this.name = name;
            this.icon = icon;
            this.selectable = selectable;
            this.renderInOverlay = renderInOverlay;
            this.internalIndex = internalIndex;
            this.cooldownTicks = cooldownTicks;
            this.pageData = pageData;
        }
    }
}
