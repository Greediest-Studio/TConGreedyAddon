package com.smd.tcongreedyaddon.integration.jei;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.ToolCore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class TraitOverviewDataProvider {

    private static final String CONARM_MOD_ID = "conarm";
    private static final String CONARM_REGISTRY_CLASS = "c4.conarm.lib.ArmoryRegistry";
    private static final String CONARM_GET_ARMOR_METHOD = "getArmor";

    private TraitOverviewDataProvider() {
    }

    static List<TraitOverviewWrapper> createRecipes() {
        List<TraitOverviewWrapper> recipes = new ArrayList<>();
        for (Item item : collectRecipeItems()) {
            ItemStack stack = buildRepresentativeStack(item);
            if (stack.isEmpty()) {
                continue;
            }

            int maxPageCount = TraitOverviewWrapper.collectVisibleTraits(stack).size();
            for (int targetTraitCount = 0; targetTraitCount <= maxPageCount; targetTraitCount++) {
                ItemStack matchingStack = buildMatchingStackForTraitCount(stack, targetTraitCount);
                int pageCount = Math.max(1, targetTraitCount);
                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    recipes.add(new TraitOverviewWrapper(item, matchingStack, matchingStack, pageIndex, pageCount));
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

    private static ItemStack buildMatchingStackForTraitCount(ItemStack sourceStack, int targetTraitCount) {
        ItemStack stack = sourceStack.copy();
        NBTTagList sourceModifiers = TagUtil.getModifiersTagList(sourceStack);
        NBTTagList modifiers = new NBTTagList();
        int visibleTraitCount = 0;

        for (int i = 0; i < sourceModifiers.tagCount(); i++) {
            NBTTagCompound modifierTag = sourceModifiers.getCompoundTagAt(i);
            if (isVisibleTraitModifier(modifierTag)) {
                if (visibleTraitCount >= targetTraitCount) {
                    continue;
                }
                visibleTraitCount++;
            }
            modifiers.appendTag(modifierTag.copy());
        }

        TagUtil.setModifiersTagList(stack, modifiers);
        return stack;
    }

    private static boolean isVisibleTraitModifier(NBTTagCompound modifierTag) {
        ModifierNBT modifierData = ModifierNBT.readTag(modifierTag);
        IModifier modifier = TinkerRegistry.getModifier(modifierData.identifier);
        return modifier instanceof ITrait && TraitOverviewWrapper.isVisibleTraitModifier((ITrait) modifier, modifier, modifierTag);
    }

    private static ItemStack buildRepresentativeStack(Item item) {
        if (!(item instanceof ToolCore)) {
            return new ItemStack(item);
        }

        ToolCore tool = (ToolCore) item;
        List<PartMaterialType> components = tool.getRequiredComponents();
        if (components.isEmpty()) {
            return new ItemStack(item);
        }

        List<Material> selectedMaterials = new ArrayList<>(components.size());
        for (PartMaterialType component : components) {
            Material material = findFirstValidMaterial(component);
            if (material == null) {
                return new ItemStack(item);
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
        return new ItemStack(item);
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
