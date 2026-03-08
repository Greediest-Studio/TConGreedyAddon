package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RightClickSpell implements IRightClickSpell {
    private final String nameKey;
    private final int cooldownTicks;
    private final ResourceLocation icon;
    private final RightClickAction action;

    @FunctionalInterface
    public interface RightClickAction {
        boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData);
    }

    private RightClickSpell(Builder builder) {
        this.nameKey = builder.nameKey;
        this.cooldownTicks = builder.cooldownTicks;
        this.icon = builder.icon;
        this.action = builder.action;
    }

    @Override
    public String getNameKey() { return nameKey; }

    @Override
    public int getCooldownTicks() { return cooldownTicks; }

    @Override
    public ResourceLocation getIcon() { return icon; }

    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData) {
        return action.onRightClick(world, player, toolStack, pageData);
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
        public RightClickSpell build() {
            if (nameKey == null || nameKey.isEmpty()) throw new IllegalStateException("Right spell must have a name");
            if (action == null) throw new IllegalStateException("Right spell must have an action");
            return new RightClickSpell(this);
        }
    }
}