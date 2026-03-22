package com.smd.tcongreedyaddon.integration.jei;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class TraitOverviewDataProvider {

    private static final int SYNTHETIC_EXTRA_TRAIT_PAGES = 1;
    private static final String CONARM_MOD_ID = "conarm";
    private static final String CONARM_REGISTRY_CLASS = "c4.conarm.lib.ArmoryRegistry";
    private static final String CONARM_GET_ARMOR_METHOD = "getArmor";

    private TraitOverviewDataProvider() {
    }

    static List<TraitOverviewWrapper> createRecipes() {
        List<TraitOverviewWrapper> recipes = new ArrayList<>();
        for (Item item : collectRecipeItems()) {
            List<ItemStack> matchingStacks = collectKnownStacks(item);
            if (matchingStacks.isEmpty()) {
                ItemStack displayStack = buildRepresentativeStack(item, matchingStacks);
                if (!displayStack.isEmpty()) {
                    matchingStacks.add(displayStack.copy());
                }
            }

            for (ItemStack stack : matchingStacks) {
                Map<Integer, ItemStack> matchingVariants = createMatchingVariants(stack);
                for (Map.Entry<Integer, ItemStack> entry : matchingVariants.entrySet()) {
                    int pageCount = entry.getKey();
                    ItemStack matchingStack = entry.getValue();
                    for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                        recipes.add(new TraitOverviewWrapper(item, stack, matchingStack, pageIndex, pageCount));
                    }
                }
            }
        }
        return recipes;
    }

    static List<Item> collectRecipeItems() {
        Set<Item> items = new LinkedHashSet<>();
        items.addAll(TinkerRegistry.getTools());
        items.addAll(collectArmoryItems());
        return new ArrayList<>(items);
    }

    private static Map<Integer, ItemStack> createMatchingVariants(ItemStack displayStack) {
        Map<Integer, ItemStack> variants = new LinkedHashMap<>();
        int baseTraitCount = TraitOverviewWrapper.collectVisibleTraits(displayStack).size();
        if (baseTraitCount <= 0) {
            return variants;
        }

        variants.put(baseTraitCount, displayStack.copy());
        for (int extraPages = 1; extraPages <= SYNTHETIC_EXTRA_TRAIT_PAGES; extraPages++) {
            int targetTraitCount = baseTraitCount + extraPages;
            ItemStack syntheticStack = buildSyntheticMatchingStack(displayStack, targetTraitCount);
            if (!syntheticStack.isEmpty()) {
                variants.put(targetTraitCount, syntheticStack);
            }
        }
        return variants;
    }

    private static ItemStack buildSyntheticMatchingStack(ItemStack sourceStack, int targetTraitCount) {
        ItemStack syntheticStack = sourceStack.copy();
        List<ITrait> currentTraits = TraitOverviewWrapper.collectVisibleTraits(syntheticStack);
        if (currentTraits.size() >= targetTraitCount) {
            return syntheticStack;
        }

        for (AbstractTrait candidate : getSyntheticTraitCandidates()) {
            if (containsTrait(currentTraits, candidate.getIdentifier())) {
                continue;
            }

            NBTTagCompound root = TagUtil.getTagSafe(syntheticStack);
            ToolBuilder.addTrait(root, candidate, 0xFFFFFF);
            syntheticStack.setTagCompound(root);

            currentTraits = TraitOverviewWrapper.collectVisibleTraits(syntheticStack);
            if (currentTraits.size() >= targetTraitCount) {
                return syntheticStack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean containsTrait(List<ITrait> traits, String identifier) {
        for (ITrait trait : traits) {
            if (trait.getIdentifier().equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    private static List<AbstractTrait> getSyntheticTraitCandidates() {
        List<AbstractTrait> traits = new ArrayList<>();
        for (IModifier modifier : TinkerRegistry.getAllModifiers()) {
            if (!(modifier instanceof AbstractTrait)) {
                continue;
            }

            AbstractTrait trait = (AbstractTrait) modifier;
            if (trait.isHidden() || com.smd.tcongreedyaddon.config.JeiTraitOverviewConfig.isTraitHidden(trait.getIdentifier())) {
                continue;
            }
            traits.add(trait);
        }
        return traits;
    }

    private static List<ItemStack> collectKnownStacks(Item item) {
        NonNullList<ItemStack> subItems = NonNullList.create();
        item.getSubItems(CreativeTabs.SEARCH, subItems);
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : subItems) {
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    private static ItemStack buildRepresentativeStack(Item item, List<ItemStack> fallbackStacks) {
        if (!(item instanceof ToolCore)) {
            return fallbackStacks.isEmpty() ? ItemStack.EMPTY : fallbackStacks.get(0).copy();
        }

        ToolCore tool = (ToolCore) item;
        List<PartMaterialType> components = tool.getRequiredComponents();
        if (components.isEmpty()) {
            return fallbackStacks.isEmpty() ? ItemStack.EMPTY : fallbackStacks.get(0).copy();
        }

        List<Material> selectedMaterials = new ArrayList<>(components.size());
        for (PartMaterialType component : components) {
            Material material = findFirstValidMaterial(component);
            if (material == null) {
                return fallbackStacks.isEmpty() ? ItemStack.EMPTY : fallbackStacks.get(0).copy();
            }
            selectedMaterials.add(material);
        }

        ItemStack bestStack = buildIfValid(tool, selectedMaterials);
        int bestTraitCount = TraitOverviewWrapper.collectVisibleTraits(bestStack).size();

        for (int pass = 0; pass < 2; pass++) {
            for (int index = 0; index < components.size(); index++) {
                PartMaterialType component = components.get(index);
                Material bestMaterial = selectedMaterials.get(index);
                for (Material candidate : TinkerRegistry.getAllMaterials()) {
                    if (!component.isValidMaterial(candidate)) {
                        continue;
                    }
                    List<Material> attempt = new ArrayList<>(selectedMaterials);
                    attempt.set(index, candidate);
                    ItemStack attemptStack = buildIfValid(tool, attempt);
                    int attemptTraitCount = TraitOverviewWrapper.collectVisibleTraits(attemptStack).size();
                    if (attemptTraitCount > bestTraitCount) {
                        bestTraitCount = attemptTraitCount;
                        bestMaterial = candidate;
                        bestStack = attemptStack;
                    }
                }
                selectedMaterials.set(index, bestMaterial);
            }
        }

        if (!bestStack.isEmpty()) {
            return bestStack;
        }
        return fallbackStacks.isEmpty() ? ItemStack.EMPTY : fallbackStacks.get(0).copy();
    }

    private static ItemStack buildIfValid(ToolCore tool, List<Material> materials) {
        ItemStack stack = tool.buildItem(materials);
        return tool.hasValidMaterials(stack) ? stack : ItemStack.EMPTY;
    }

    private static Material findFirstValidMaterial(PartMaterialType component) {
        for (Material material : TinkerRegistry.getAllMaterials()) {
            if (component.isValidMaterial(material)) {
                return material;
            }
        }
        return null;
    }

    private static List<Item> collectArmoryItems() {
        if (!Loader.isModLoaded(CONARM_MOD_ID)) {
            return Collections.emptyList();
        }

        try {
            Class<?> registryClass = Class.forName(CONARM_REGISTRY_CLASS);
            Method getArmorMethod = registryClass.getMethod(CONARM_GET_ARMOR_METHOD);
            Object result = getArmorMethod.invoke(null);
            if (result instanceof Collection<?>) {
                List<Item> items = new ArrayList<>();
                for (Object entry : (Collection<?>) result) {
                    if (entry instanceof Item) {
                        items.add((Item) entry);
                    }
                }
                return items;
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            TConGreedyAddon.LOGGER.warn("Failed to load Construct's Armory armor items for JEI trait overview.", e);
        }

        return Collections.emptyList();
    }
}