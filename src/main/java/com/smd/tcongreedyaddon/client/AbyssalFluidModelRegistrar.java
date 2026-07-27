package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.plugin.abyssalcraft.abyssalcraft;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public final class AbyssalFluidModelRegistrar {

    private static final ResourceLocation FLUID_MODEL = new ResourceLocation(Tags.MOD_ID, "fluid_block");

    private AbyssalFluidModelRegistrar() {
    }

    public static void registerModels() {
        registerModel(abyssalcraft.abyssalniteFluid);
        registerModel(abyssalcraft.liquifiedCoraliumFluid);
        registerModel(abyssalcraft.dreadiumFluid);
    }

    private static void registerModel(Fluid fluid) {
        if (fluid == null || fluid.getBlock() == null) {
            return;
        }

        Block block = fluid.getBlock();
        FluidStateMapper mapper = new FluidStateMapper(fluid);
        ModelLoader.setCustomStateMapper(block, mapper);

        Item item = Item.getItemFromBlock(block);
        if (item != Items.AIR) {
            ModelLoader.registerItemVariants(item);
            ModelLoader.setCustomMeshDefinition(item, mapper);
        }
    }

    private static final class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {

        private final ModelResourceLocation model;

        private FluidStateMapper(Fluid fluid) {
            model = new ModelResourceLocation(FLUID_MODEL, fluid.getName());
        }

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            return model;
        }

        @Nonnull
        @Override
        public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) {
            return model;
        }
    }
}
