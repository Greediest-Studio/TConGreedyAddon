package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.Item;

import javax.annotation.Nonnull;
import java.util.List;

@mezz.jei.api.JEIPlugin
public class TraitOverviewJeiPlugin implements IModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistry registry) {
        TraitOverviewToolSubtypeInterpreter interpreter = new TraitOverviewToolSubtypeInterpreter();
        for (Item item : TraitOverviewDataProvider.collectRecipeItems()) {
            registry.registerSubtypeInterpreter(item, interpreter);
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new TraitOverviewCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        List<TraitOverviewWrapper> recipes = TraitOverviewDataProvider.createRecipes();
        registry.addRecipes(recipes, TraitOverviewCategory.UID);
    }
}