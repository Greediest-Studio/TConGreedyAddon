package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import net.minecraft.item.ItemStack;

public class TraitOverviewToolSubtypeInterpreter implements ISubtypeInterpreter {

    @Override
    public String apply(ItemStack itemStack) {
        return itemStack.getItemDamage() + ":" + TraitOverviewWrapper.collectVisibleTraits(itemStack).size();
    }
}
