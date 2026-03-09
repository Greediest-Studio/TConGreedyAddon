package com.smd.tcongreedyaddon.tools.magicbook.gui;


import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class MagicBookGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // x 参数传递手部索引（0=主手，1=副手）
        EnumHand hand = x == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof MagicBook) {
            return new ContainerMagicBook(player.inventory, stack);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        EnumHand hand = x == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof MagicBook) {
            return new GuiMagicBook(player.inventory, stack);
        }
        return null;
    }
}