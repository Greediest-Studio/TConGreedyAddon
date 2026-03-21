package com.smd.tcongreedyaddon.event;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

public abstract class SpellUseEvent extends Event {

    private final EntityPlayer player;
    private final ItemStack bookStack;
    private final ItemStack pageStack;
    private final MagicPageItem.SlotType slotType;
    private final int bookmarkId;
    private final int spellRawIndex;
    private final String spellId;
    private final TriggerSource triggerSource;

    protected SpellUseEvent(EntityPlayer player, ItemStack bookStack, ItemStack pageStack,
                            MagicPageItem.SlotType slotType, int bookmarkId,
                            int spellRawIndex, String spellId, TriggerSource triggerSource) {
        this.player = player;
        this.bookStack = bookStack;
        this.pageStack = pageStack;
        this.slotType = slotType;
        this.bookmarkId = bookmarkId;
        this.spellRawIndex = spellRawIndex;
        this.spellId = spellId;
        this.triggerSource = triggerSource;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public ItemStack getBookStack() {
        return bookStack;
    }

    public ItemStack getPageStack() {
        return pageStack;
    }

    public MagicPageItem.SlotType getSlotType() {
        return slotType;
    }

    public int getBookmarkId() {
        return bookmarkId;
    }

    public int getSpellRawIndex() {
        return spellRawIndex;
    }

    public String getSpellId() {
        return spellId;
    }

    public TriggerSource getTriggerSource() {
        return triggerSource;
    }

    @Nullable
    public Boolean getSuccess() {
        return null;
    }

    @Cancelable
    public static class Pre extends SpellUseEvent {
        public Pre(EntityPlayer player, ItemStack bookStack, ItemStack pageStack,
                   MagicPageItem.SlotType slotType, int bookmarkId,
                   int spellRawIndex, String spellId, TriggerSource triggerSource) {
            super(player, bookStack, pageStack, slotType, bookmarkId, spellRawIndex, spellId, triggerSource);
        }
    }

    public static class Post extends SpellUseEvent {
        private final boolean success;

        public Post(EntityPlayer player, ItemStack bookStack, ItemStack pageStack,
                    MagicPageItem.SlotType slotType, int bookmarkId,
                    int spellRawIndex, String spellId, TriggerSource triggerSource,
                    boolean success) {
            super(player, bookStack, pageStack, slotType, bookmarkId, spellRawIndex, spellId, triggerSource);
            this.success = success;
        }

        @Override
        public Boolean getSuccess() {
            return success;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    public static class Finish extends SpellUseEvent {
        private final boolean success;
        private final int heldTicks;
        private final boolean interrupted;
        private final boolean canceled;

        public Finish(EntityPlayer player, ItemStack bookStack, ItemStack pageStack,
                      MagicPageItem.SlotType slotType, int bookmarkId,
                      int spellRawIndex, String spellId, TriggerSource triggerSource,
                      boolean success, int heldTicks, boolean interrupted, boolean canceled) {
            super(player, bookStack, pageStack, slotType, bookmarkId, spellRawIndex, spellId, triggerSource);
            this.success = success;
            this.heldTicks = heldTicks;
            this.interrupted = interrupted;
            this.canceled = canceled;
        }

        @Override
        public Boolean getSuccess() {
            return success;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getHeldTicks() {
            return heldTicks;
        }

        public boolean isInterrupted() {
            return interrupted;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }
}
