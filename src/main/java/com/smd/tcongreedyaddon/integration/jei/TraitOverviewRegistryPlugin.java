package com.smd.tcongreedyaddon.integration.jei;

import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.shared.TinkerCommons;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
class TraitOverviewRegistryPlugin implements IRecipeRegistryPlugin {

    static final TraitOverviewRegistryPlugin INSTANCE = new TraitOverviewRegistryPlugin();

    private TraitOverviewRegistryPlugin() {
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if (focus.getValue() instanceof ItemStack) {
            ItemStack stack = (ItemStack) focus.getValue();
            if (stack.getItem() == TinkerCommons.book || TraitOverviewEntryCollector.hasVisibleEntries(stack)) {
                return Collections.singletonList(TraitOverviewCategory.UID);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (recipeCategory instanceof TraitOverviewCategory && focus.getValue() instanceof ItemStack) {
            ItemStack stack = ((ItemStack) focus.getValue()).copy();
            if (!stack.isEmpty()) {
                stack.setCount(1);
                if (stack.getItem() == TinkerCommons.book) {
                    return Collections.singletonList(cast(new TraitOverviewWrapper(stack, Collections.emptyList(), true, true)));
                }

                List<TraitOverviewEntry> entries = TraitOverviewEntryCollector.collectEntries(stack);
                if (!entries.isEmpty()) {
                    return Collections.singletonList(cast(new TraitOverviewWrapper(stack, entries, false, false)));
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        if (recipeCategory instanceof TraitOverviewCategory) {
            return Collections.singletonList(cast(new TraitOverviewWrapper(new ItemStack(TinkerCommons.book), Collections.emptyList(), true, true)));
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static <T extends IRecipeWrapper> T cast(IRecipeWrapper wrapper) {
        return (T) wrapper;
    }
}
