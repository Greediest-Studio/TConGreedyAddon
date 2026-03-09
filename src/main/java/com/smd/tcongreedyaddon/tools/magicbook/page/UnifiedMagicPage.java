package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ILeftClickSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IPassiveSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IRightClickSpell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.ArrayList;
import java.util.List;

public class UnifiedMagicPage extends MagicPageItem {

    private final SlotType slotType;
    private final List<ILeftClickSpell> leftSpells = new ArrayList<>();
    private final List<IRightClickSpell> rightSpells = new ArrayList<>();
    private final List<IPassiveSpell> passives = new ArrayList<>();
    private String displayNameKey = "unified.page.default";

    protected UnifiedMagicPage(Builder builder) {
        this.slotType = builder.slotType;
        this.leftSpells.addAll(builder.leftSpells);
        this.rightSpells.addAll(builder.rightSpells);
        this.passives.addAll(builder.passives);
        if (builder.displayNameKey != null) this.displayNameKey = builder.displayNameKey;
        setMaxStackSize(1);
        setCreativeTab(TinkerRegistry.tabParts);
    }

    @Override
    public SlotType getSlotType() {
        return slotType;
    }

    @Override
    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target, NBTTagCompound pageData) {
        if (player.world.isRemote) return true;
        if (slotType != SlotType.LEFT || leftSpells.isEmpty()) return false;
        int index = pageData.getInteger("spellIndex");
        if (index < 0 || index >= leftSpells.size()) index = 0;
        ILeftClickSpell spell = leftSpells.get(index);
        return spell.onLeftClick(toolStack, player, target);
    }

    @Override
    public int getSpellCount(SlotType slotType) {
        return slotType == SlotType.LEFT ? leftSpells.size() : rightSpells.size();
    }

    @Override
    public String getSpellDisplayName(int internalIndex, SlotType slotType) {
        List<?> spells = slotType == SlotType.LEFT ? leftSpells : rightSpells;
        if (internalIndex < 0 || internalIndex >= spells.size()) return "Unknown";
        Object spell = spells.get(internalIndex);
        String key = (spell instanceof ILeftClickSpell) ? ((ILeftClickSpell) spell).getNameKey() : ((IRightClickSpell) spell).getNameKey();
        return I18n.translateToLocal(key);
    }

    @Override
    public int getSpellCooldownTicks(int internalIndex, SlotType slotType) {
        List<?> spells = slotType == SlotType.LEFT ? leftSpells : rightSpells;
        if (internalIndex < 0 || internalIndex >= spells.size()) return 0;
        Object spell = spells.get(internalIndex);
        if (spell instanceof ILeftClickSpell) {
            return ((ILeftClickSpell) spell).getCooldownTicks();
        } else if (spell instanceof IRightClickSpell) {
            return ((IRightClickSpell) spell).getCooldownTicks();
        }
        return 0;
    }

    // --- 右键逻辑 ---
    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData) {
        if (slotType != SlotType.RIGHT || rightSpells.isEmpty()) return false;
        int index = pageData.getInteger("spellIndex");
        if (index < 0 || index >= rightSpells.size()) index = 0;
        IRightClickSpell spell = rightSpells.get(index);
        // 冷却检查（魔导书已实现）
        return spell.onRightClick(world, player, toolStack, pageData);
    }

    // --- 被动更新 ---
    @Override
    public void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, SlotType slot) {
        for (IPassiveSpell effect : passives) {
            if (!effect.runOnClient() && world.isRemote) continue;
            if (effect.runOnClient() && !world.isRemote) continue;
            if (effect.getInterval() > 0 && player.ticksExisted % effect.getInterval() != 0) continue;
            effect.onHeldUpdate(world, player, toolStack, pageData);
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
            String key = (spell instanceof ILeftClickSpell) ? ((ILeftClickSpell) spell).getNameKey() : ((IRightClickSpell) spell).getNameKey();
            return I18n.translateToLocal(key);
        }
        return I18n.translateToLocal(displayNameKey);
    }

    @Override
    public List<String> getAllSpellNames(NBTTagCompound pageData) {
        List<?> spells = slotType == SlotType.LEFT ? leftSpells : rightSpells;
        List<String> names = new ArrayList<>();
        for (Object spell : spells) {
            String key = (spell instanceof ILeftClickSpell) ? ((ILeftClickSpell) spell).getNameKey() : ((IRightClickSpell) spell).getNameKey();
            names.add(I18n.translateToLocal(key));
        }
        return names;
    }

    private boolean isOnCooldown(ItemStack toolStack, NBTTagCompound pageData, int index, boolean left) {
        return false;
    }

    private void setCooldown(ItemStack toolStack, NBTTagCompound pageData, int index, boolean left) {
    }

    // --- 建造者 ---
    public static class Builder {
        private final SlotType slotType;
        private final List<ILeftClickSpell> leftSpells = new ArrayList<>();
        private final List<IRightClickSpell> rightSpells = new ArrayList<>();
        private final List<IPassiveSpell> passives = new ArrayList<>();
        private String displayNameKey;

        public Builder(SlotType slotType) {
            this.slotType = slotType;
        }

        public Builder addLeftSpell(ILeftClickSpell spell) {
            leftSpells.add(spell);
            return this;
        }

        public Builder addRightSpell(IRightClickSpell spell) {
            rightSpells.add(spell);
            return this;
        }

        public Builder addPassive(IPassiveSpell spell) {
            passives.add(spell);
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
            if (spell instanceof ILeftClickSpell) {
                icons.add(((ILeftClickSpell) spell).getIcon());
            } else if (spell instanceof IRightClickSpell) {
                icons.add(((IRightClickSpell) spell).getIcon());
            }
        }
        return icons;
    }

    @Override
    public int getSpellCooldownTicks(int spellIndex) {
        List<?> spells = (slotType == SlotType.LEFT) ? leftSpells : rightSpells;
        if (spellIndex >= 0 && spellIndex < spells.size()) {
            Object spell = spells.get(spellIndex);
            if (spell instanceof ILeftClickSpell) {
                return ((ILeftClickSpell) spell).getCooldownTicks();
            } else if (spell instanceof IRightClickSpell) {
                return ((IRightClickSpell) spell).getCooldownTicks();
            }
        }
        return 0;
    }
}