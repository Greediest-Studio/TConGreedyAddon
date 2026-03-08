package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class PassiveSpell implements IPassiveSpell {
    private final HeldUpdateAction action;
    private final int interval;
    private final boolean runOnClient;

    @FunctionalInterface
    public interface HeldUpdateAction {
        void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData);
    }

    private PassiveSpell(Builder builder) {
        this.action = builder.action;
        this.interval = builder.interval;
        this.runOnClient = builder.runOnClient;
    }

    @Override
    public void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData) {
        action.onHeldUpdate(world, player, toolStack, pageData);
    }

    @Override
    public int getInterval() { return interval; }

    @Override
    public boolean runOnClient() { return runOnClient; }

    public static class Builder {
        private HeldUpdateAction action;
        private int interval = 0;
        private boolean runOnClient = false;

        public Builder action(HeldUpdateAction action) { this.action = action; return this; }
        public Builder interval(int ticks) { this.interval = ticks; return this; }
        public Builder runOnClient(boolean runOnClient) { this.runOnClient = runOnClient; return this; }
        public PassiveSpell build() {
            if (action == null) throw new IllegalStateException("Passive spell must have an action");
            return new PassiveSpell(this);
        }
    }
}
