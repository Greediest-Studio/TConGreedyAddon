package com.smd.tcongreedyaddon.util;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.ToolCore;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TicToolStacks {

    private static final String MOD_TCONSTRUCT = "tconstruct";
    private static final String MOD_CONARM = "conarm";
    private static final String MOD_TCONGREEDY = "tcongreedyaddon";

    private static final Map<String, EntityEquipmentSlot> ARMOR_IDS;
    private static volatile Set<Item> tinkerTools;

    static {
        Map<String, EntityEquipmentSlot> armorIds = new HashMap<>();
        armorIds.put("conarm:helmet", EntityEquipmentSlot.HEAD);
        armorIds.put("conarm:chestplate", EntityEquipmentSlot.CHEST);
        armorIds.put("conarm:leggings", EntityEquipmentSlot.LEGS);
        armorIds.put("conarm:boots", EntityEquipmentSlot.FEET);
        ARMOR_IDS = Collections.unmodifiableMap(armorIds);
    }

    private TicToolStacks() {
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static ResourceLocation getRegistryName(ItemStack stack) {
        if (isEmpty(stack) || stack.getItem() == null) {
            return null;
        }
        return stack.getItem().getRegistryName();
    }

    public static String getRegistryId(ItemStack stack) {
        ResourceLocation id = getRegistryName(stack);
        return id == null ? "" : id.toString();
    }

    public static boolean isTicTool(ItemStack stack) {
        if (isEmpty(stack)) {
            return false;
        }

        Item item = stack.getItem();
        if (item instanceof ToolCore) {
            return true;
        }

        if (getTinkerTools().contains(item)) {
            return true;
        }

        ResourceLocation id = getRegistryName(stack);
        if (id == null) {
            return false;
        }

        String namespace = id.getNamespace();
        if (MOD_TCONSTRUCT.equals(namespace) || MOD_TCONGREEDY.equals(namespace)) {
            return hasTicData(stack);
        }

        return false;
    }

    public static boolean isTicTarget(ItemStack stack) {
        return isTicTool(stack) || isTicArmor(stack);
    }

    public static boolean isTicArmor(ItemStack stack) {
        ResourceLocation id = getRegistryName(stack);
        return id != null && ARMOR_IDS.containsKey(id.toString());
    }

    public static EntityEquipmentSlot getArmorSlot(ItemStack stack) {
        ResourceLocation id = getRegistryName(stack);
        return id == null ? null : ARMOR_IDS.get(id.toString());
    }

    public static String getArmorType(ItemStack stack) {
        EntityEquipmentSlot slot = getArmorSlot(stack);
        if (slot == null) {
            return "";
        }
        switch (slot) {
            case HEAD:
                return "helmet";
            case CHEST:
                return "chestplate";
            case LEGS:
                return "leggings";
            case FEET:
                return "boots";
            default:
                return "";
        }
    }

    public static EntityEquipmentSlot parseArmorSlot(String slotName) {
        if (slotName == null) {
            return null;
        }
        switch (slotName.trim().toLowerCase(Locale.ROOT)) {
            case "head":
            case "helmet":
                return EntityEquipmentSlot.HEAD;
            case "chest":
            case "chestplate":
                return EntityEquipmentSlot.CHEST;
            case "legs":
            case "leggings":
                return EntityEquipmentSlot.LEGS;
            case "feet":
            case "boots":
                return EntityEquipmentSlot.FEET;
            default:
                return null;
        }
    }

    public static int armorSlotIndex(EntityEquipmentSlot slot) {
        if (slot == null) {
            return -1;
        }
        switch (slot) {
            case HEAD:
                return 0;
            case CHEST:
                return 1;
            case LEGS:
                return 2;
            case FEET:
                return 3;
            default:
                return -1;
        }
    }

    public static ItemStack[] getAllKnownTicItems() {
        Set<Item> items = new HashSet<>(getTinkerTools());
        for (String id : ARMOR_IDS.keySet()) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(id));
            if (item != null) {
                items.add(item);
            }
        }

        ItemStack[] stacks = new ItemStack[items.size()];
        int index = 0;
        for (Item item : items) {
            stacks[index++] = new ItemStack(item);
        }
        return stacks;
    }

    private static Set<Item> getTinkerTools() {
        Set<Item> cached = tinkerTools;
        if (cached != null) {
            return cached;
        }

        Set<Item> tools = new HashSet<>();
        for (ToolCore tool : TinkerRegistry.getTools()) {
            tools.add(tool);
        }
        tinkerTools = Collections.unmodifiableSet(tools);
        return tinkerTools;
    }

    private static boolean hasTicData(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        return stack.getTagCompound().hasKey("TinkerData") || stack.getTagCompound().hasKey("Stats");
    }
}
