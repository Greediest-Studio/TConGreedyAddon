package com.smd.tcongreedyaddon.integration.jei;

import com.smd.tcongreedyaddon.config.JeiTraitOverviewConfig;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;

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
        return hasSameBaseSubtype(matchingStack, focusStack);
    }

    private static boolean hasSameBaseSubtype(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        return left.getItem() == right.getItem()
            && left.getItemDamage() == right.getItemDamage()
            && collectVisibleTraits(left).size() == collectVisibleTraits(right).size();
    }

    static List<ITrait> collectVisibleTraits(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, ITrait> traitsById = new LinkedHashMap<>();
        NBTTagList modifiers = TagUtil.getModifiersTagList(stack);
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound modifierTag = modifiers.getCompoundTagAt(i);
            ModifierNBT modifierData = ModifierNBT.readTag(modifierTag);
            IModifier modifier = TinkerRegistry.getModifier(modifierData.identifier);
            if (!(modifier instanceof ITrait)) {
                continue;
            }

            ITrait trait = (ITrait) modifier;
            if (!isVisibleTraitModifier(trait, modifier, modifierTag)) {
                continue;
            }

            traitsById.putIfAbsent(trait.getIdentifier(), trait);
        }
        return new ArrayList<>(traitsById.values());
    }

    static boolean isVisibleTraitModifier(ITrait trait, IModifier modifier, NBTTagCompound modifierTag) {
        if (modifier.isHidden() || trait.isHidden() || JeiTraitOverviewConfig.isTraitHidden(trait.getIdentifier())) {
            return false;
        }

        String tooltip = modifier.getTooltip(modifierTag, false);
        String plainTooltip = TextFormatting.getTextWithoutFormattingCodes(tooltip);
        return plainTooltip != null && !plainTooltip.trim().isEmpty();
    }
}
