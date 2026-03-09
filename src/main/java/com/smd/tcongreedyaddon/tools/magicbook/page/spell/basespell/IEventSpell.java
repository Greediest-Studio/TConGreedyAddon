package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.List;

public interface IEventSpell {
    /**
     * 当事件触发时调用
     * @param event      Forge事件对象
     * @param player     持有书的玩家
     * @param bookStack  魔法书物品
     * @param pageStack  当前书签物品
     * @param pageData   书签的NBT数据
     * @param slot       书签所在槽位类型
     * @return  true表示法术成功执行（可触发耐久消耗等）
     */
    boolean onEvent(Event event, EntityPlayer player, ItemStack bookStack, ItemStack pageStack, NBTTagCompound pageData, MagicPageItem.SlotType slot);

    /**
     * 可选：返回该书签关心的事件类型列表，用于优化性能（只有匹配的事件才会调用onEvent）
     */
    default List<Class<? extends Event>> getListeningEvents() {
        return Collections.emptyList();
    }
}