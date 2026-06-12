package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

class TraitOverviewWrapper implements IRecipeWrapper {

    private final ItemStack displayStack;
    private final List<TraitOverviewEntry> entries;
    private final boolean emptyState;
    private final boolean triggerPage;

    TraitOverviewWrapper(ItemStack displayStack, List<TraitOverviewEntry> entries, boolean emptyState, boolean triggerPage) {
        this.displayStack = displayStack.copy();
        this.entries = Collections.unmodifiableList(entries);
        this.emptyState = emptyState;
        this.triggerPage = triggerPage;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        if (!displayStack.isEmpty()) {
            List<ItemStack> stacks = Collections.singletonList(displayStack);
            ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(stacks));
            ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(stacks));
        }
    }

    ItemStack getDisplayStack() {
        return displayStack;
    }

    @Nonnull
    List<TraitOverviewEntry> getEntries() {
        return entries;
    }

    boolean isEmptyState() {
        return emptyState;
    }

    boolean isTriggerPage() {
        return triggerPage;
    }
}
