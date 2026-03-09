package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnifiedMagicPage extends MagicPageItem {

    private final SlotType slotType;
    private final List<ISpell> leftSpells = new ArrayList<>();
    private final List<ISpell> rightSpells = new ArrayList<>();
    private final Map<Class<? extends Event>, List<ISpell>> eventSpellMap = new HashMap<>(); // 事件映射
    private String displayNameKey = "unified.page.default";

    protected UnifiedMagicPage(Builder builder) {
        this.slotType = builder.slotType;
        this.leftSpells.addAll(builder.leftSpells);
        this.rightSpells.addAll(builder.rightSpells);
        if (builder.displayNameKey != null) this.displayNameKey = builder.displayNameKey;

        // 构建事件映射
        buildEventSpellMap(leftSpells);
        buildEventSpellMap(rightSpells);

        setMaxStackSize(1);
        setCreativeTab(TinkerRegistry.tabParts);
    }

    private void buildEventSpellMap(List<ISpell> spells) {
        for (ISpell spell : spells) {
            List<Class<? extends Event>> events = spell.getListeningEvents();
            for (Class<? extends Event> eventClass : events) {
                eventSpellMap.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(spell);
            }
        }
    }

    @Override
    public SlotType getSlotType() {
        return slotType;
    }

    // ==================== 索引管理 ====================

    private List<ISpell> getSelectableSpells(SlotType slot) {
        List<ISpell> list = slot == SlotType.LEFT ? leftSpells : rightSpells;
        return list.stream().filter(ISpell::isSelectable).collect(Collectors.toList());
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

    // ==================== 主动施法 ====================

    @Override
    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target, NBTTagCompound pageData, ItemStack pageStack) {
        if (player.world.isRemote) return false;

        int index = pageData.getInteger("spellIndex");
        List<ISpell> selectable = getSelectableSpells(SlotType.LEFT);
        if (index < 0 || index >= selectable.size()) return false;
        ISpell spell = selectable.get(index);

        if (isSpellOnCooldown(pageStack, index, player.world)) return false;

        SpellContext context = new SpellContext(player.world, player, toolStack, pageStack, pageData, SlotType.LEFT, TriggerSource.leftClick(), target);
        if (!spell.canTrigger(context)) return false;

        boolean success = spell.execute(context);
        if (success) {
            setSpellCooldown(pageStack, index, player.world);
            pageStack.setTagCompound(pageData);
        }
        return success;
    }

    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, ItemStack pageStack) {
        if (world.isRemote) return false;

        int index = pageData.getInteger("spellIndex");
        List<ISpell> selectable = getSelectableSpells(SlotType.RIGHT);
        if (index < 0 || index >= selectable.size()) return false;
        ISpell spell = selectable.get(index);

        if (isSpellOnCooldown(pageStack, index, world)) return false;

        SpellContext context = new SpellContext(world, player, toolStack, pageStack, pageData, SlotType.RIGHT, TriggerSource.rightClick(), null);
        if (!spell.canTrigger(context)) return false;

        boolean success = spell.execute(context);
        if (success) {
            setSpellCooldown(pageStack, index, world);
            pageStack.setTagCompound(pageData);
        }
        return success;
    }

    // ==================== 被动更新 ====================

    @Override
    public void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, SlotType slot, ItemStack pageStack) {
        List<ISpell> allSpells = slot == SlotType.LEFT ? leftSpells : rightSpells;
        for (ISpell spell : allSpells) {
            SpellContext context = new SpellContext(world, player, toolStack, pageStack, pageData, slot, TriggerSource.tick(), null);
            if (spell.canTrigger(context)) {
                spell.execute(context);
            }
        }
        pageStack.setTagCompound(pageData);
    }

    // ==================== 事件触发（优化版）====================

    public void onEvent(Event event, EntityPlayer player, ItemStack bookStack, ItemStack pageStack, NBTTagCompound pageData, SlotType slot) {
        List<ISpell> spells = eventSpellMap.get(event.getClass());
        if (spells == null) return;

        TriggerSource source = TriggerSource.event(event);
        for (ISpell spell : spells) {
            SpellContext context = new SpellContext(player.world, player, bookStack, pageStack, pageData, slot, source, null);
            if (spell.canTrigger(context)) {
                spell.execute(context);
            }
        }
        pageStack.setTagCompound(pageData);
    }

    @Override
    public int getInitialSpellIndex(ItemStack pageStack) {
        return 0;
    }

    // ==================== 显示名称 ====================

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
        return getSelectableSpells(slot).stream()
                .map(s -> I18n.translateToLocal(s.getNameKey()))
                .collect(Collectors.toList());
    }

    // ==================== 工具提示 ====================

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        // 左槽法术（可切换）
        List<ISpell> leftSelectable = getSelectableSpells(SlotType.LEFT);
        if (!leftSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.left_spells") + ":");
            for (int i = 0; i < leftSelectable.size(); i++) {
                String name = I18n.translateToLocal(leftSelectable.get(i).getNameKey());
                tooltip.add(" - " + name);
            }
        }

        // 右槽法术（可切换）
        List<ISpell> rightSelectable = getSelectableSpells(SlotType.RIGHT);
        if (!rightSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.right_spells") + ":");
            for (int i = 0; i < rightSelectable.size(); i++) {
                String name = I18n.translateToLocal(rightSelectable.get(i).getNameKey());
                tooltip.add(" - " + name);
            }
        }

        // 不可切换的法术（被动/事件）
        List<ISpell> leftAll = leftSpells;
        List<ISpell> leftNonSelectable = leftAll.stream().filter(s -> !s.isSelectable()).collect(Collectors.toList());
        if (!leftNonSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.left_passive") + ":");
            for (ISpell s : leftNonSelectable) {
                tooltip.add(" - " + I18n.translateToLocal(s.getNameKey()));
            }
        }

        List<ISpell> rightAll = rightSpells;
        List<ISpell> rightNonSelectable = rightAll.stream().filter(s -> !s.isSelectable()).collect(Collectors.toList());
        if (!rightNonSelectable.isEmpty()) {
            tooltip.add(I18n.translateToLocal("tooltip.right_passive") + ":");
            for (ISpell s : rightNonSelectable) {
                tooltip.add(" - " + I18n.translateToLocal(s.getNameKey()));
            }
        }
    }

    public List<ResourceLocation> getSpellIcons(NBTTagCompound pageData, ItemStack pageStack) {
        SlotType slot = getSlotType();
        return getSelectableSpells(slot).stream()
                .map(ISpell::getIcon)
                .collect(Collectors.toList());
    }

    // ==================== 建造者 ====================

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
            return new UnifiedMagicPage(this);
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
                    spell.getIcon(),
                    spell.isSelectable(),
                    spell.shouldRenderInOverlay(),
                    i,
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
        public final int internalIndex;
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