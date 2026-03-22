package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.ToolCore;

import javax.annotation.Nonnull;
import java.util.List;

@mezz.jei.api.JEIPlugin
public class TraitOverviewJeiPlugin implements IModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistry registry) {
        TraitOverviewToolSubtypeInterpreter interpreter = new TraitOverviewToolSubtypeInterpreter();
        for (ToolCore tool : TinkerRegistry.getTools()) {
            registry.registerSubtypeInterpreter(tool, interpreter);
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