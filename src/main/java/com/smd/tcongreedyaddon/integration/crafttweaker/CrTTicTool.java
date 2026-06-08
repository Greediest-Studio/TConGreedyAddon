package com.smd.tcongreedyaddon.integration.crafttweaker;

import com.smd.tcongreedyaddon.util.TicArmorTraitCache;
import com.smd.tcongreedyaddon.util.TicToolNbt;
import com.smd.tcongreedyaddon.util.TicToolStacks;
import com.smd.tcongreedyaddon.util.TicToolStats;
import com.smd.tcongreedyaddon.util.TicToolTraits;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.entity.IEntityEquipmentSlot;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.tcongreedy.TicTool")
public final class CrTTicTool {

    private CrTTicTool() {
    }

    @ZenMethod
    public static boolean isTool(IItemStack stack) {
        return TicToolStacks.isTicTool(toStack(stack));
    }

    @ZenMethod
    public static IItemStack[] getAllItems() {
        return CraftTweakerMC.getIItemStacks(TicToolStacks.getAllKnownTicItems());
    }

    @ZenMethod
    public static boolean isArmor(IItemStack stack) {
        return TicToolStacks.isTicArmor(toStack(stack));
    }

    @ZenMethod
    public static String getArmorType(IItemStack stack) {
        return TicToolStacks.getArmorType(toStack(stack));
    }

    @ZenMethod
    public static IEntityEquipmentSlot getArmorSlot(IItemStack stack) {
        EntityEquipmentSlot slot = TicToolStacks.getArmorSlot(toStack(stack));
        return slot == null ? null : CraftTweakerMC.getIEntityEquipmentSlot(slot);
    }

    @ZenMethod
    public static String[] getMaterials(IItemStack stack) {
        ItemStack mcStack = toStack(stack);
        if (!TicToolStacks.isTicTarget(mcStack)) {
            return TicToolNbt.EMPTY_STRINGS;
        }
        return TicToolNbt.readStringList(TicToolNbt.getTinkerData(mcStack), "Materials");
    }

    @ZenMethod
    public static boolean setBroken(IItemStack stack, boolean broken) {
        return TicToolStats.setBroken(toStack(stack), broken);
    }

    @ZenMethod
    public static boolean addMiningSpeed(IItemStack stack, float amount, String token) {
        return TicToolStats.addMiningSpeed(toStack(stack), amount, token);
    }

    @ZenMethod
    public static boolean addAttack(IItemStack stack, float amount, String token) {
        return TicToolStats.addAttack(toStack(stack), amount, token);
    }

    @ZenMethod
    public static boolean addFreeModifiers(IItemStack stack, int amount, String token) {
        return TicToolStats.addFreeModifiers(toStack(stack), amount, token);
    }

    @ZenMethod
    public static boolean addDefense(IItemStack stack, float amount, String token) {
        return TicToolStats.addDefense(toStack(stack), amount, token);
    }

    @ZenMethod
    public static boolean addToughness(IItemStack stack, float amount, String token) {
        return TicToolStats.addToughness(toStack(stack), amount, token);
    }

    @ZenMethod
    public static boolean addHarvestLevel(IItemStack stack, int amount, String token) {
        return TicToolStats.addHarvestLevel(toStack(stack), amount, token);
    }

    @ZenMethod
    public static boolean addDrawSpeed(IItemStack stack, float amount, String token) {
        return TicToolStats.addDrawSpeed(toStack(stack), amount, token);
    }

    @ZenMethod
    public static boolean addAttackSpeedMultiplier(IItemStack stack, float amount, String token) {
        return TicToolStats.addAttackSpeedMultiplier(toStack(stack), amount, token);
    }

    @ZenMethod
    public static String[] getTraits(IItemStack stack) {
        return TicToolTraits.getTraits(toStack(stack));
    }

    @ZenMethod
    public static boolean hasTrait(IItemStack stack, String traitId) {
        return TicToolTraits.hasTrait(toStack(stack), traitId);
    }

    @ZenMethod
    public static int getTraitColor(IItemStack stack, String traitId) {
        return TicToolTraits.getTraitColor(toStack(stack), traitId);
    }

    @ZenMethod
    public static int getTraitLevel(IItemStack stack, String traitId) {
        return TicToolTraits.getTraitLevel(toStack(stack), traitId);
    }

    @ZenMethod
    public static boolean addTrait(IItemStack stack, String traitId, int color, int level) {
        return TicToolTraits.addTrait(toStack(stack), traitId, color, level);
    }

    @ZenMethod
    public static boolean removeTrait(IItemStack stack, String traitId) {
        return TicToolTraits.removeTrait(toStack(stack), traitId);
    }

    @ZenMethod
    public static IItemStack withTrait(IItemStack stack, String traitId, int color, int level) {
        return CraftTweakerMC.getIItemStack(TicToolTraits.withTrait(toStack(stack), traitId, color, level));
    }

    @ZenMethod
    public static IItemStack withoutTrait(IItemStack stack, String traitId) {
        return CraftTweakerMC.getIItemStack(TicToolTraits.withoutTrait(toStack(stack), traitId));
    }

    @ZenMethod
    public static String[] getArmorTraits(IPlayer player) {
        return TicArmorTraitCache.INSTANCE.getArmorTraits(toPlayer(player));
    }

    @ZenMethod
    public static String[] getArmorSlotTraits(IPlayer player, String slotName) {
        return TicArmorTraitCache.INSTANCE.getArmorSlotTraits(toPlayer(player), slotName);
    }

    @ZenMethod
    public static boolean hasArmorTrait(IPlayer player, String traitId) {
        return TicArmorTraitCache.INSTANCE.hasArmorTrait(toPlayer(player), traitId);
    }

    @ZenMethod
    public static boolean hasArmorSlotTrait(IPlayer player, String slotName, String traitId) {
        return TicArmorTraitCache.INSTANCE.hasArmorSlotTrait(toPlayer(player), slotName, traitId);
    }

    @ZenMethod
    public static boolean refreshArmorCache(IPlayer player) {
        return TicArmorTraitCache.INSTANCE.refresh(toPlayer(player));
    }

    private static ItemStack toStack(IItemStack stack) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        Object internal = stack.getInternal();
        return internal instanceof ItemStack ? (ItemStack) internal : ItemStack.EMPTY;
    }

    private static EntityPlayer toPlayer(IPlayer player) {
        return player == null ? null : CraftTweakerMC.getPlayer(player);
    }
}
