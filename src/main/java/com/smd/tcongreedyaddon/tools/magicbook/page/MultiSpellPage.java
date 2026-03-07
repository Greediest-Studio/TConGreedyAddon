package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
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
        public final ResourceLocation icon;
        public final SpellAction action;

        private Spell(Builder builder) {
            this.name = builder.name;
            this.cooldownTicks = builder.cooldownTicks;
            this.icon = builder.icon;
            this.action = builder.action;
        }

        public static class Builder {
            private String name;
            private int cooldownTicks;
            private ResourceLocation icon;
            private SpellAction action;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder cooldown(int ticks) {
                this.cooldownTicks = ticks;
                return this;
            }

            public Builder icon(ResourceLocation icon) {
                this.icon = icon;
                return this;
            }

            public Builder action(SpellAction action) {
                this.action = action;
                return this;
            }

            public Spell build() {

                if (name == null || name.isEmpty()) {
                    throw new IllegalStateException("Spell must have a non-empty name");
                }
                if (action == null) {
                    throw new IllegalStateException("Spell must have an action");
                }
                return new Spell(this);
            }
        }
    }

    public MultiSpellPage() {
        registerSpells();
    }

    protected abstract void registerSpells();

    protected void addSpell(Spell.Builder builder) {
        spells.add(builder.build());
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

    @Override
    public String getCurrentSpellDisplayName(NBTTagCompound pageData) {
        int index = pageData.getInteger("spellIndex");
        if (index >= 0 && index < spells.size()) {
            String key = spells.get(index).name;

            return I18n.translateToLocal(key);
        }
        return "Unknown";
    }

    @Override
    public List<String> getAllSpellNames(NBTTagCompound pageData) {
        List<String> names = new ArrayList<>();
        for (Spell spell : spells) {
            names.add(I18n.translateToLocal(spell.name));
        }
        return names;
    }

    public ResourceLocation getCurrentSpellIcon(NBTTagCompound pageData) {
        int index = pageData.getInteger("spellIndex");
        if (index >= 0 && index < spells.size()) {
            return spells.get(index).icon;
        }
        return null;
    }

    public List<ResourceLocation> getSpellIconTextures() {
        List<ResourceLocation> icons = new ArrayList<>();
        for (Spell spell : spells) {
            icons.add(spell.icon);
        }
        return icons;
    }
}