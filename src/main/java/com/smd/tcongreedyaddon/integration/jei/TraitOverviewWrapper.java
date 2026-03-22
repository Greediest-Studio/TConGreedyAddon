package com.smd.tcongreedyaddon.integration.jei;

import com.smd.tcongreedyaddon.config.JeiTraitOverviewConfig;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TraitOverviewWrapper implements IRecipeWrapper {

    private final Item toolItem;
    private final ItemStack displayStack;
    private final ItemStack matchingStack;
    private final int pageIndex;
    private final int registeredPageCount;
    private ItemStack focusedStack = ItemStack.EMPTY;

    TraitOverviewWrapper(Item toolItem, ItemStack displayStack, ItemStack matchingStack, int pageIndex, int registeredPageCount) {
        this.toolItem = toolItem;
        this.displayStack = displayStack.copy();
        this.matchingStack = matchingStack.copy();
        this.pageIndex = pageIndex;
        this.registeredPageCount = Math.max(1, registeredPageCount);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> stacks = Collections.singletonList(matchingStack);
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(stacks));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(stacks));
    }

    ItemStack getDisplayStack() {
        return getActiveStack();
    }

    List<ITrait> getDisplayTraits() {
        return collectVisibleTraits(getActiveStack());
    }

    @Nullable
    ITrait getCurrentTrait() {
        List<ITrait> traits = getDisplayTraits();
        if (pageIndex < 0 || pageIndex >= traits.size()) {
            return null;
        }
        return traits.get(pageIndex);
    }

    int getCurrentPage() {
        return Math.min(pageIndex + 1, Math.max(1, getPageCount()));
    }

    int getPageCount() {
        return registeredPageCount;
    }

    void updateFocus(@Nullable ItemStack focusStack) {
        if (focusStack == null || focusStack.isEmpty()) {
            focusedStack = ItemStack.EMPTY;
            return;
        }

        if (focusStack.getItem() != toolItem) {
            focusedStack = ItemStack.EMPTY;
            return;
        }

        focusedStack = hasMatchingSubtype(focusStack) ? focusStack.copy() : ItemStack.EMPTY;
    }

    private ItemStack getActiveStack() {
        return focusedStack.isEmpty() ? displayStack : focusedStack;
    }

    private boolean hasMatchingSubtype(ItemStack focusStack) {
        return hasSameBaseSubtype(matchingStack, focusStack)
                && collectVisibleTraits(focusStack).size() == registeredPageCount;
    }

    private static boolean hasSameBaseSubtype(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        if (left.getItem() != right.getItem() || left.getItemDamage() != right.getItemDamage()) {
            return false;
        }

        NBTTagList leftMaterials = TagUtil.getBaseMaterialsTagList(left);
        NBTTagList rightMaterials = TagUtil.getBaseMaterialsTagList(right);
        if (leftMaterials.tagCount() != rightMaterials.tagCount()) {
            return false;
        }

        for (int i = 0; i < leftMaterials.tagCount(); i++) {
            if (!leftMaterials.getStringTagAt(i).equals(rightMaterials.getStringTagAt(i))) {
                return false;
            }
        }
        return true;
    }

    static List<ITrait> collectVisibleTraits(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, ITrait> traitsById = new LinkedHashMap<>();
        for (ITrait trait : TinkerUtil.getTraitsOrdered(stack)) {
            if (trait == null || trait.isHidden() || JeiTraitOverviewConfig.isTraitHidden(trait.getIdentifier())) {
                continue;
            }
            traitsById.putIfAbsent(trait.getIdentifier(), trait);
        }
        return new ArrayList<>(traitsById.values());
    }
}