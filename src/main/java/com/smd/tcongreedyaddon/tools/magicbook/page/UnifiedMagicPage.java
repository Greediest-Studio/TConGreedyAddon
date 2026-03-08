package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class UnifiedMagicPage extends MagicPageItem {

    @FunctionalInterface
    public interface LeftClickAction {
        boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target);
    }

    @FunctionalInterface
    public interface RightClickAction {
        boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData);
    }

    @FunctionalInterface
    public interface HeldUpdateAction {
        void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData);
    }

    // --- 内部数据类 ---
    public static class LeftSpell {
        public final String nameKey;
        public final int cooldownTicks;
        public final ResourceLocation icon;
        public final LeftClickAction action;

        private LeftSpell(Builder builder) {
            this.nameKey = builder.nameKey;
            this.cooldownTicks = builder.cooldownTicks;
            this.icon = builder.icon;
            this.action = builder.action;
        }

        public static class Builder {
            private String nameKey;
            private int cooldownTicks;
            private ResourceLocation icon;
            private LeftClickAction action;

            public Builder name(String nameKey) { this.nameKey = nameKey; return this; }
            public Builder cooldown(int ticks) { this.cooldownTicks = ticks; return this; }
            public Builder icon(ResourceLocation icon) { this.icon = icon; return this; }
            public Builder action(LeftClickAction action) { this.action = action; return this; }
            public LeftSpell build() {
                if (nameKey == null || nameKey.isEmpty()) throw new IllegalStateException("Left spell must have a name");
                if (action == null) throw new IllegalStateException("Left spell must have an action");
                return new LeftSpell(this);
            }
        }
    }

    public static class RightSpell {
        public final String nameKey;
        public final int cooldownTicks;
        public final ResourceLocation icon;
        public final RightClickAction action;

        private RightSpell(Builder builder) {
            this.nameKey = builder.nameKey;
            this.cooldownTicks = builder.cooldownTicks;
            this.icon = builder.icon;
            this.action = builder.action;
        }

        public static class Builder {
            private String nameKey;
            private int cooldownTicks;
            private ResourceLocation icon;
            private RightClickAction action;

            public Builder name(String nameKey) { this.nameKey = nameKey; return this; }
            public Builder cooldown(int ticks) { this.cooldownTicks = ticks; return this; }
            public Builder icon(ResourceLocation icon) { this.icon = icon; return this; }
            public Builder action(RightClickAction action) { this.action = action; return this; }
            public RightSpell build() {
                if (nameKey == null || nameKey.isEmpty()) throw new IllegalStateException("Right spell must have a name");
                if (action == null) throw new IllegalStateException("Right spell must have an action");
                return new RightSpell(this);
            }
        }
    }

    public static class PassiveEffect {
        public final HeldUpdateAction action;
        public final int interval;          // 触发间隔（tick），0 表示每 tick
        public final boolean runOnClient;

        private PassiveEffect(Builder builder) {
            this.action = builder.action;
            this.interval = builder.interval;
            this.runOnClient = builder.runOnClient;
        }

        public static class Builder {
            private HeldUpdateAction action;
            private int interval = 0;
            private boolean runOnClient = false;

            public Builder action(HeldUpdateAction action) { this.action = action; return this; }
            public Builder interval(int ticks) { this.interval = ticks; return this; }
            public Builder runOnClient(boolean runOnClient) { this.runOnClient = runOnClient; return this; }
            public PassiveEffect build() {
                if (action == null) throw new IllegalStateException("Passive effect must have an action");
                return new PassiveEffect(this);
            }
        }
    }

    // --- 页面数据 ---
    private final SlotType slotType;
    private final List<LeftSpell> leftSpells = new ArrayList<>();
    private final List<RightSpell> rightSpells = new ArrayList<>();
    private final List<PassiveEffect> passives = new ArrayList<>();
    private String displayNameKey = "unified.page.default"; // 备用显示名称

    protected UnifiedMagicPage(Builder builder) {
        this.slotType = builder.slotType;
        this.leftSpells.addAll(builder.leftSpells);
        this.rightSpells.addAll(builder.rightSpells);
        this.passives.addAll(builder.passives);
        if (builder.displayNameKey != null) this.displayNameKey = builder.displayNameKey;
        setMaxStackSize(1);
        setCreativeTab(slimeknights.tconstruct.library.TinkerRegistry.tabParts);
    }

    @Override
    public SlotType getSlotType() {
        return slotType;
    }

    // --- 左键逻辑 ---
    @Override
    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target) {
        if (player.world.isRemote) return true;
        if (slotType != SlotType.LEFT || leftSpells.isEmpty()) return false;
        NBTTagCompound pageData = getPageData(toolStack); // 需要魔导书提供获取页面数据的方法，此处简化
        int index = pageData.getInteger("spellIndex");
        if (index < 0 || index >= leftSpells.size()) index = 0;
        LeftSpell spell = leftSpells.get(index);
        // 冷却检查（需魔导书支持）
        if (!isOnCooldown(toolStack, pageData, index, true)) {
            boolean result = spell.action.onLeftClick(toolStack, player, target);
            if (result) setCooldown(toolStack, pageData, index, true);
            return result;
        }
        return false;
    }

    // --- 右键逻辑 ---
    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData) {
        if (slotType != SlotType.RIGHT || rightSpells.isEmpty()) return false;
        int index = pageData.getInteger("spellIndex");
        if (index < 0 || index >= rightSpells.size()) index = 0;
        RightSpell spell = rightSpells.get(index);
        // 冷却检查（魔导书已实现）
        return spell.action.onRightClick(world, player, toolStack, pageData);
    }

    // --- 被动更新 ---
    @Override
    public void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, SlotType slot) {
        for (PassiveEffect effect : passives) {
            if (!effect.runOnClient && world.isRemote) continue;
            if (effect.runOnClient && !world.isRemote) continue;
            if (effect.interval > 0 && player.ticksExisted % effect.interval != 0) continue;
            effect.action.onHeldUpdate(world, player, toolStack, pageData);
        }
    }

    @Override
    public void nextSpell(ItemStack toolStack, NBTTagCompound pageData) {
        List<?> spells = slotType == SlotType.LEFT ? leftSpells : rightSpells;
        if (spells.isEmpty()) return;
        int current = pageData.getInteger("spellIndex");
        int next = (current + 1) % spells.size();
        pageData.setInteger("spellIndex", next);
    }

    @Override
    public int getInitialSpellIndex(ItemStack pageStack) {
        return 0;
    }

    // --- 显示名称 ---
    @Override
    public String getCurrentSpellDisplayName(NBTTagCompound pageData) {
        List<?> spells = slotType == SlotType.LEFT ? leftSpells : rightSpells;
        if (spells.isEmpty()) return I18n.translateToLocal(displayNameKey);
        int index = pageData.getInteger("spellIndex");
        if (index >= 0 && index < spells.size()) {
            Object spell = spells.get(index);
            String key = (spell instanceof LeftSpell) ? ((LeftSpell) spell).nameKey : ((RightSpell) spell).nameKey;
            return I18n.translateToLocal(key);
        }
        return I18n.translateToLocal(displayNameKey);
    }

    @Override
    public List<String> getAllSpellNames(NBTTagCompound pageData) {
        List<?> spells = slotType == SlotType.LEFT ? leftSpells : rightSpells;
        List<String> names = new ArrayList<>();
        for (Object spell : spells) {
            String key = (spell instanceof LeftSpell) ? ((LeftSpell) spell).nameKey : ((RightSpell) spell).nameKey;
            names.add(I18n.translateToLocal(key));
        }
        return names;
    }

    // --- 辅助方法（需要魔导书配合）---
    private NBTTagCompound getPageData(ItemStack toolStack) {
        // 实际应从 toolStack 的 NBT 中提取当前页面的数据，此处简化返回空
        return new NBTTagCompound();
    }

    private boolean isOnCooldown(ItemStack toolStack, NBTTagCompound pageData, int index, boolean left) {
        // 需魔导书提供冷却机制，参考右槽实现
        return false;
    }

    private void setCooldown(ItemStack toolStack, NBTTagCompound pageData, int index, boolean left) {
        // 需魔导书支持
    }

    // --- 建造者 ---
    public static class Builder {
        private final SlotType slotType;
        private final List<LeftSpell> leftSpells = new ArrayList<>();
        private final List<RightSpell> rightSpells = new ArrayList<>();
        private final List<PassiveEffect> passives = new ArrayList<>();
        private String displayNameKey;

        public Builder(SlotType slotType) {
            this.slotType = slotType;
        }

        public Builder addLeftSpell(LeftSpell.Builder spellBuilder) {
            leftSpells.add(spellBuilder.build());
            return this;
        }

        public Builder addRightSpell(RightSpell.Builder spellBuilder) {
            rightSpells.add(spellBuilder.build());
            return this;
        }

        public Builder addPassive(PassiveEffect.Builder effectBuilder) {
            passives.add(effectBuilder.build());
            return this;
        }

        public Builder displayName(String key) {
            this.displayNameKey = key;
            return this;
        }

        public UnifiedMagicPage build() {
            // 验证：左槽不能有右键法术，右槽不能有左键法术
            if (slotType == SlotType.LEFT && !rightSpells.isEmpty()) {
                throw new IllegalStateException("Left page cannot have right spells");
            }
            if (slotType == SlotType.RIGHT && !leftSpells.isEmpty()) {
                throw new IllegalStateException("Right page cannot have left spells");
            }
            return new UnifiedMagicPage(this);
        }
    }

    public List<ResourceLocation> getSpellIcons(NBTTagCompound pageData) {
        List<ResourceLocation> icons = new ArrayList<>();
        List<?> spells = (slotType == SlotType.LEFT) ? leftSpells : rightSpells;
        for (Object spell : spells) {
            if (spell instanceof LeftSpell) {
                icons.add(((LeftSpell) spell).icon);
            } else if (spell instanceof RightSpell) {
                icons.add(((RightSpell) spell).icon);
            }
        }
        return icons;
    }

    @Override
    public int getSpellCooldownTicks(int spellIndex) {
        List<?> spells = (slotType == SlotType.LEFT) ? leftSpells : rightSpells;
        if (spellIndex >= 0 && spellIndex < spells.size()) {
            Object spell = spells.get(spellIndex);
            if (spell instanceof LeftSpell) {
                return ((LeftSpell) spell).cooldownTicks;
            } else if (spell instanceof RightSpell) {
                return ((RightSpell) spell).cooldownTicks;
            }
        }
        return 0;
    }
}