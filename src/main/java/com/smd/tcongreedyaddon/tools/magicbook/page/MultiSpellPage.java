package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiSpellPage extends MagicPageItem {

    protected final List<Spell> spells = new ArrayList<>();

    @FunctionalInterface
    public interface SpellAction {
        boolean cast(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData);
    }

    public static class Spell {
        public final String name;
        public final int cooldownTicks;
        public final SpellAction action;

        public Spell(String name, int cooldownTicks, SpellAction action) {
            this.name = name;
            this.cooldownTicks = cooldownTicks;
            this.action = action;
        }
    }

    public MultiSpellPage() {
        registerSpells();
    }

    protected abstract void registerSpells();

    protected void addSpell(String name, int cooldownTicks, SpellAction action) {
        spells.add(new Spell(name, cooldownTicks, action));
    }

    @Override
    public SlotType getSlotType() {
        return SlotType.RIGHT;
    }

    @Override
    public int getSpellCooldownTicks(int spellIndex) {
        if (spellIndex >= 0 && spellIndex < spells.size()) {
            return spells.get(spellIndex).cooldownTicks;
        }
        return 0;
    }

    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData) {
        int index = pageData.getInteger("spellIndex");
        if (index >= 0 && index < spells.size()) {
            return spells.get(index).action.cast(world, player, toolStack, pageData);
        }
        return false;
    }

    @Override
    public void nextSpell(ItemStack toolStack, NBTTagCompound pageData) {
        int current = pageData.getInteger("spellIndex");
        int next = (current + 1) % spells.size();
        pageData.setInteger("spellIndex", next);
    }

    @Override
    public int getInitialSpellIndex(ItemStack pageStack) {
        return 0;
    }

    /**
     * 获取当前法术的显示名称（用于工具提示）
     */
    public String getCurrentSpellDisplayName(NBTTagCompound pageData) {
        int index = pageData.getInteger("spellIndex");
        if (index >= 0 && index < spells.size()) {
            return spells.get(index).name;
        }
        return "Unknown";
    }

    @Override
    public List<String> getAllSpellNames(NBTTagCompound pageData) {
        List<String> names = new ArrayList<>();
        for (Spell spell : spells) {
            names.add(spell.name);
        }
        return names;
    }
}