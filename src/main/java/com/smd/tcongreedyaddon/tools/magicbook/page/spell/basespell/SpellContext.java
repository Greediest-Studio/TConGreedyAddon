package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import com.smd.tcongreedyaddon.util.MagicBookHelper;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SpellContext {
    public final World world;
    public final EntityPlayer player;
    public final ItemStack bookStack;
    public final ItemStack pageStack;
    public final NBTTagCompound pageData;
    public final MagicPageItem.SlotType slot;
    public final TriggerSource trigger;
    @Nullable public final Entity target;

    private Float cachedRange;
    private Float cachedCritChance;
    private Integer cachedSpellSpeed;
    private Integer cachedLeftSlots;
    private Integer cachedRightSlots;

    public SpellContext(World world, EntityPlayer player, ItemStack bookStack,
                        ItemStack pageStack, NBTTagCompound pageData,
                        MagicPageItem.SlotType slot, TriggerSource trigger,
                        @Nullable Entity target) {
        this.world = world;
        this.player = player;
        this.bookStack = bookStack;
        this.pageStack = pageStack;
        this.pageData = pageData;
        this.slot = slot;
        this.trigger = trigger;
        this.target = target;
    }

    public float getRange() {
        if (cachedRange == null) {
            Float range = MagicBookHelper.getRange(bookStack);
            cachedRange = range != null ? range : MagicBook.BEAM_RANGE;
        }
        return cachedRange;
    }

    public float getCritChance() {
        if (cachedCritChance == null) {
            Float chance = MagicBookHelper.getCritChance(bookStack);
            cachedCritChance = chance != null ? chance : 0.0f;
        }
        return cachedCritChance;
    }

    public int getSpellSpeed() {
        if (cachedSpellSpeed == null) {
            Integer speed = MagicBookHelper.getSpellSpeed(bookStack);
            cachedSpellSpeed = speed != null ? speed : 1;
        }
        return cachedSpellSpeed;
    }

    public int getLeftSlotCount() {
        if (cachedLeftSlots == null) {
            Integer count = MagicBookHelper.getLeftSlotCount(bookStack);
            cachedLeftSlots = count != null ? count : 1;
        }
        return cachedLeftSlots;
    }

    public int getRightSlotCount() {
        if (cachedRightSlots == null) {
            Integer count = MagicBookHelper.getRightSlotCount(bookStack);
            cachedRightSlots = count != null ? count : 1;
        }
        return cachedRightSlots;
    }

    public boolean isLeftSlot() {
        return slot == MagicPageItem.SlotType.LEFT;
    }

    public boolean isRightSlot() {
        return slot == MagicPageItem.SlotType.RIGHT;
    }

    public int getCurrentSlotCount() {
        return isLeftSlot() ? getLeftSlotCount() : getRightSlotCount();
    }
}