package com.smd.tcongreedyaddon.tools.oldweapons;

import com.google.common.collect.ImmutableSet;
import com.smd.tcongreedyaddon.plugin.oldweapons.OldWeapons;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.AoeToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.*;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.List;

public class AllInOneTool extends AoeToolCore {

    public static final ImmutableSet<net.minecraft.block.material.Material> effectiveMaterials =
            ImmutableSet.of(net.minecraft.block.material.Material.ROCK,
                    net.minecraft.block.material.Material.IRON,
                    net.minecraft.block.material.Material.ANVIL,
                    net.minecraft.block.material.Material.GLASS,
                    net.minecraft.block.material.Material.GRASS,
                    net.minecraft.block.material.Material.GROUND,
                    net.minecraft.block.material.Material.CLAY,
                    net.minecraft.block.material.Material.WOOD,
                    net.minecraft.block.material.Material.PLANTS,
                    net.minecraft.block.material.Material.VINE,
                    net.minecraft.block.material.Material.GOURD);

    public AllInOneTool() {
        super(PartMaterialType.handle(TinkerTools.toughToolRod),
                PartMaterialType.head(TinkerTools.pickHead),
                PartMaterialType.head(TinkerTools.broadAxeHead),
                PartMaterialType.head(TinkerTools.shovelHead),
                PartMaterialType.extra(OldWeapons.greatbladeCore));

        addCategory(Category.HARVEST);
        this.setHarvestLevel("pickaxe", 0);
        this.setHarvestLevel("axe", 0);
        this.setHarvestLevel("shovel", 0);
        this.setHarvestLevel("allinonetool", 0);
        setTranslationKey("allinonetool").setRegistryName("allinonetool");
    }

    @Override
    public boolean isEffective(IBlockState state) {
        return effectiveMaterials.contains(state.getMaterial());
    }

    @Override
    public float miningSpeedModifier() {
        return 0.8f;
    }

    @Override
    public float damagePotential() {
        return 0.0f;
    }

    @Override
    public double attackSpeed() {
        return 0.1f;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{1, 2, 3};
    }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase player, Entity entity, float damage) {

        return super.dealDamage(stack, player, entity, damage * 1/100);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.GRASS || block == Blocks.DIRT) {
            world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
            return EnumActionResult.SUCCESS;
        }

        if (player.isSneaking() && block == Blocks.GRASS) {
            world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState());
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    protected int getPickLevel(ItemStack stack) {
        return new AllInOneToolNBT(TagUtil.getToolTag(stack)).pickLevel;
    }

    protected int getAxeLevel(ItemStack stack) {
        return new AllInOneToolNBT(TagUtil.getToolTag(stack)).axeLevel;
    }

    protected int getShovelLevel(ItemStack stack) {
        return new AllInOneToolNBT(TagUtil.getToolTag(stack)).shovelLevel;
    }

    @Override
    public List<String> getInformation(ItemStack stack, boolean detailed) {
        TooltipBuilder info = new TooltipBuilder(stack);

        info.addDurability(!detailed);

        String text = Util.translate("stat.mattock.picklevel.name");
        info.add(String.format("%s: %s", text, HarvestLevels.getHarvestLevelName(getPickLevel(stack))) + TextFormatting.RESET);

        text = Util.translate("stat.mattock.axelevel.name");
        info.add(String.format("%s: %s", text, HarvestLevels.getHarvestLevelName(getAxeLevel(stack))) + TextFormatting.RESET);

        text = Util.translate("stat.mattock.shovellevel.name");
        info.add(String.format("%s: %s", text, HarvestLevels.getHarvestLevelName(getShovelLevel(stack))) + TextFormatting.RESET);

        info.addMiningSpeed();
        info.addAttack();

        if (ToolHelper.getFreeModifiers(stack) > 0) {
            info.addFreeModifiers();
        }

        if (detailed) {
            info.addModifierInfo();
        }

        return info.getTooltip();
    }



    @Override
    public ToolNBT buildTagData(java.util.List<Material> materials) {
        HandleMaterialStats handle = materials.get(0).getStatsOrUnknown(MaterialTypes.HANDLE);
        HeadMaterialStats pick = materials.get(1).getStatsOrUnknown(MaterialTypes.HEAD);
        HeadMaterialStats axe = materials.get(2).getStatsOrUnknown(MaterialTypes.HEAD);
        HeadMaterialStats shovel = materials.get(3).getStatsOrUnknown(MaterialTypes.HEAD);

        AllInOneToolNBT data = new AllInOneToolNBT();
        data.head(pick, axe, shovel);
        data.handle(handle);
        data.durability *= 0.8;

        data.pickLevel = pick.harvestLevel;
        data.axeLevel = axe.harvestLevel;
        data.shovelLevel = shovel.harvestLevel;

        return data;
    }

    public static class AllInOneToolNBT extends ToolNBT {
        private static final String TAG_PickLevel = Tags.HARVESTLEVEL + "Pick";
        private static final String TAG_AxeLevel = Tags.HARVESTLEVEL + "Axe";
        private static final String TAG_ShovelLevel = Tags.HARVESTLEVEL + "Shovel";

        public int pickLevel;
        public int axeLevel;
        public int shovelLevel;

        public AllInOneToolNBT() {}

        public AllInOneToolNBT(NBTTagCompound tag) {
            super(tag);
        }

        @Override
        public void read(NBTTagCompound tag) {
            super.read(tag);
            pickLevel = tag.getInteger(TAG_PickLevel);
            axeLevel = tag.getInteger(TAG_AxeLevel);
            shovelLevel = tag.getInteger(TAG_ShovelLevel);
        }

        @Override
        public void write(NBTTagCompound tag) {
            super.write(tag);
            tag.setInteger(TAG_PickLevel, pickLevel);
            tag.setInteger(TAG_AxeLevel, axeLevel);
            tag.setInteger(TAG_ShovelLevel, shovelLevel);
        }
    }
}

