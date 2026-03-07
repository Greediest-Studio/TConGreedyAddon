package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.Collections;
import java.util.List;

public abstract class SimpleLeftPage extends MagicPageItem {

    @FunctionalInterface
    public interface LeftClickAction {
        boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target);
    }

    private LeftClickAction action = (toolStack, player, target) -> false;
    private String spellNameKey = "disabled";

    protected void setLeftClickAction(LeftClickAction action) {
        this.action = action;
    }

    protected void setSpellNameKey(String key) {
        this.spellNameKey = key;
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

    @Override
    public String getCurrentSpellDisplayName(NBTTagCompound pageData) {
        return net.minecraft.util.text.translation.I18n.translateToLocal(spellNameKey);
    }

    @Override
    public List<String> getAllSpellNames(NBTTagCompound pageData) {
        return Collections.singletonList(getCurrentSpellDisplayName(pageData));
    }

    @Override
    public void nextSpell(ItemStack toolStack, NBTTagCompound pageData) {

    }

    @Override
    public int getSpellCooldownTicks(int spellIndex) {
        return 0;
    }
}