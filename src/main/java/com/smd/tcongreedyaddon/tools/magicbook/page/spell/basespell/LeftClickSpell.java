package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LeftClickSpell implements ILeftClickSpell {
    private final String nameKey;
    private final int cooldownTicks;
    private final ResourceLocation icon;
    private final LeftClickAction action;

    @FunctionalInterface
    public interface LeftClickAction {
        boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target);
    }

    private LeftClickSpell(Builder builder) {
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
    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target) {
        return action.onLeftClick(toolStack, player, target);
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
        public LeftClickSpell build() {
            if (nameKey == null || nameKey.isEmpty()) throw new IllegalStateException("Left spell must have a name");
            if (action == null) throw new IllegalStateException("Left spell must have an action");
            return new LeftClickSpell(this);
        }
    }
}