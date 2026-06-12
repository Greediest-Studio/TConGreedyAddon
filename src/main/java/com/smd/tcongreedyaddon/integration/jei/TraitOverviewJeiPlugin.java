package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.shared.TinkerCommons;

import javax.annotation.Nonnull;

@JEIPlugin
public class TraitOverviewJeiPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new TraitOverviewCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addRecipeCatalyst(new ItemStack(TinkerCommons.book), TraitOverviewCategory.UID);
        registry.addRecipeRegistryPlugin(TraitOverviewRegistryPlugin.INSTANCE);
    }
}
