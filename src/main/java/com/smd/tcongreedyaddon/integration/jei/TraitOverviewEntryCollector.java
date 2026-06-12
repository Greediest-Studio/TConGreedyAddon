package com.smd.tcongreedyaddon.integration.jei;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.config.JeiTraitOverviewConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.IToolMod;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class TraitOverviewEntryCollector {

    private TraitOverviewEntryCollector() {
    }

    static boolean hasVisibleEntries(ItemStack stack) {
        return !collectEntries(stack).isEmpty();
    }

    static List<TraitOverviewEntry> collectEntries(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }

        NBTTagList modifiers = TagUtil.getModifiersTagList(stack);
        if (modifiers.tagCount() == 0) {
            return Collections.emptyList();
        }

        List<TraitOverviewEntry> entries = new ArrayList<>();
        Set<String> seenIdentifiers = new LinkedHashSet<>();
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound modifierTag = modifiers.getCompoundTagAt(i);
            ModifierNBT modifierData = ModifierNBT.readTag(modifierTag);
            IModifier modifier = TinkerRegistry.getModifier(modifierData.identifier);
            if (modifier == null) {
                continue;
            }

            if (!isVisibleEntry(modifier)) {
                continue;
            }

            String identifier = modifier.getIdentifier();
            if (!seenIdentifiers.add(identifier)) {
                continue;
            }

            entries.add(new TraitOverviewEntry(
                identifier,
                getDisplayName(modifier),
                buildMaintainedDescriptionKey(identifier),
                getDescriptionText(modifier),
                buildJeiDescriptionKey(identifier),
                resolveSourceModId(modifier),
                modifier instanceof ITrait ? TraitOverviewEntry.EntryKind.TRAIT : TraitOverviewEntry.EntryKind.MODIFIER
            ));
        }

        return entries;
    }

    private static boolean isVisibleEntry(IModifier modifier) {
        return !modifier.isHidden() && !JeiTraitOverviewConfig.isEntryHidden(modifier.getIdentifier());
    }

    private static String getDisplayName(IToolMod toolMod) {
        String localizedName = toolMod.getLocalizedName();
        return isUsableLocalization(localizedName) ? localizedName : toolMod.getIdentifier();
    }

    private static boolean isUsableLocalization(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String getDescriptionText(IToolMod toolMod) {
        String localizedDesc = toolMod.getLocalizedDesc();
        if (isUsableLocalization(localizedDesc) && !localizedDesc.equals(buildMaintainedDescriptionKey(toolMod.getIdentifier()))) {
            return localizedDesc;
        }
        return buildMaintainedDescriptionKey(toolMod.getIdentifier());
    }

    private static String buildMaintainedDescriptionKey(String identifier) {
        return "modifier." + identifier + ".desc";
    }

    private static String buildJeiDescriptionKey(String identifier) {
        return "tcongreedyaddon.jei.modifier." + identifier + ".text";
    }

    private static String resolveSourceModId(IModifier modifier) {
        String identifier = modifier.getIdentifier();
        int namespaceSeparator = identifier.indexOf(':');
        if (namespaceSeparator > 0) {
            return identifier.substring(0, namespaceSeparator);
        }

        if (ForgeRegistries.ITEMS != null) {
            String packageName = modifier.getClass().getName().toLowerCase(Locale.ROOT);
            if (packageName.contains("conarm")) {
                return "conarm";
            }
            if (packageName.contains("tconstruct") || packageName.contains("slimeknights")) {
                return "tconstruct";
            }
            if (packageName.contains("tcongreedyaddon") || packageName.contains(Tags.MOD_ID.toLowerCase(Locale.ROOT))) {
                return Tags.MOD_ID;
            }
        }

        return "unknown";
    }
}
