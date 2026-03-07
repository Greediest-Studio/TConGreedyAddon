package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

public abstract class SimpleLeftPage extends MagicPageItem {

    @FunctionalInterface
    public interface LeftClickAction {
        boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target);
    }

    private LeftClickAction action = (toolStack, player, target) -> false;

    /**
     * 设置左键攻击的行为。建议在行为内部调用 ToolHelper.attackEntity 以保持匠魂的攻击机制。
     */
    protected void setLeftClickAction(LeftClickAction action) {
        this.action = action;
    }

    @Override
    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target) {
        if (player.world.isRemote) return true;
        return action.onLeftClick(toolStack, player, target);
    }

    @Override
    public SlotType getSlotType() {
        return SlotType.LEFT;
    }

    protected boolean performStandardAttack(ItemStack toolStack, EntityPlayer player, Entity target) {
        if (toolStack.getItem() instanceof TinkerToolCore) {
            return ToolHelper.attackEntity(toolStack, (TinkerToolCore) toolStack.getItem(), player, target);
        }
        return false;
    }
}