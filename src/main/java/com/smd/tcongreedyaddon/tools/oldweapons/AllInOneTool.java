package com.smd.tcongreedyaddon.tools.oldweapons;

import com.google.common.collect.ImmutableSet;
import com.smd.tcongreedyaddon.plugin.oldweapons.oldweapons;
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
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
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

    public static final ImmutableSet<net.minecraft.block.material.Material> effectiveMaterialsPick =
            ImmutableSet.of(net.minecraft.block.material.Material.ROCK,
                    net.minecraft.block.material.Material.IRON,
                    net.minecraft.block.material.Material.ANVIL,
                    net.minecraft.block.material.Material.GLASS);

    public static final ImmutableSet<net.minecraft.block.material.Material> effectiveMaterialsAxe =
            ImmutableSet.of(net.minecraft.block.material.Material.WOOD,
                    net.minecraft.block.material.Material.CACTUS,
                    net.minecraft.block.material.Material.PLANTS,
                    net.minecraft.block.material.Material.VINE,
                    net.minecraft.block.material.Material.GOURD);

    public static final ImmutableSet<net.minecraft.block.material.Material> effectiveMaterialsShovel =
            ImmutableSet.of(net.minecraft.block.material.Material.GRASS,
                    net.minecraft.block.material.Material.GROUND,
                    net.minecraft.block.material.Material.SAND,
                    net.minecraft.block.material.Material.CRAFTED_SNOW,
                    net.minecraft.block.material.Material.SNOW,
                    net.minecraft.block.material.Material.CLAY,
                    net.minecraft.block.material.Material.CAKE);

    public static final ImmutableSet<Block> effectiveBlocksShovel =
            ImmutableSet.of(Blocks.CLAY,
                    Blocks.DIRT,
                    Blocks.FARMLAND,
                    Blocks.GRASS,
                    Blocks.GRASS_PATH,
                    Blocks.GRAVEL,
                    Blocks.MYCELIUM,
                    Blocks.SAND,
                    Blocks.SNOW,
                    Blocks.SNOW_LAYER,
                    Blocks.SOUL_SAND,
                    Blocks.CONCRETE_POWDER);

    public AllInOneTool() {
        super(PartMaterialType.handle(TinkerTools.toughToolRod),
                PartMaterialType.head(TinkerTools.pickHead),
                PartMaterialType.head(TinkerTools.broadAxeHead),
                PartMaterialType.head(TinkerTools.shovelHead),
                PartMaterialType.extra(oldweapons.greatbladeCore));

        addCategory(Category.HARVEST);
        this.setHarvestLevel("pickaxe", 0);
        this.setHarvestLevel("axe", 0);
        this.setHarvestLevel("shovel", 0);
        this.setHarvestLevel("allinonetool", 0);
        setTranslationKey("allinonetool").setRegistryName("allinonetool");
    }

    @Override
    public boolean isEffective(IBlockState state) {
        return effectiveMaterialsPick.contains(state.getMaterial())
                || effectiveMaterialsAxe.contains(state.getMaterial())
                || effectiveMaterialsShovel.contains(state.getMaterial())
                || effectiveBlocksShovel.contains(state.getBlock());
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
        return 1f;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{1, 2, 3};
    }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase player, Entity entity, float damage) {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return true;
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return this.doMakePath(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }
        return this.doTill(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player,
                               @Nullable IBlockState blockState) {
        if (StringUtils.isNullOrEmpty(toolClass)) {
            return -1;
        }
        switch (toolClass) {
            case "pickaxe":
                return this.getPickLevel(stack);
            case "axe":
                return this.getAxeLevel(stack);
            case "shovel":
                return this.getShovelLevel(stack);
        }
        return super.getHarvestLevel(stack, toolClass, player, blockState);
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
        ExtraMaterialStats core = materials.get(4).getStatsOrUnknown(MaterialTypes.EXTRA);

        AllInOneToolNBT data = new AllInOneToolNBT();
        data.head(pick, axe, shovel);
        data.handle(handle);
        data.extra(core);
        data.durability = Math.max(1, Math.round(data.durability * 0.8f));
        data.attack = 1.0f;

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

