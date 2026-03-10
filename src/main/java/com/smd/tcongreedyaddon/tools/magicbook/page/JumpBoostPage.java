package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.List;

public class JumpBoostPage extends UnifiedMagicPage {

    public JumpBoostPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(new JumpBoostSpell())
                .displayName("jump_boost_page")
        );
        setRegistryName("jump_boost_page");
        setTranslationKey("jump_boost_page");
    }

    private static class JumpBoostSpell implements ISpell {

        @Override
        public List<Class<? extends Event>> getListeningEvents() {
            return Collections.singletonList(LivingEvent.LivingJumpEvent.class);
        }

        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isEvent() &&
                    context.trigger.getEvent() instanceof LivingEvent.LivingJumpEvent &&
                    context.slot == MagicPageItem.SlotType.RIGHT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (!context.world.isRemote) {
                EntityPlayer player = context.player;
                player.motionY += 1.0;
                player.velocityChanged = true;
            }
            return true;
        }

        @Override
        public boolean isSelectable() {
            return false;
        }

        @Override
        public String getNameKey() {
            return "spell.jump_boost";
        }

        @Override
        public ResourceLocation getIcon() {
            return null;
        }

        @Override
        public int getCooldownTicks() {
            return 80;
        }

        @Override
        public boolean shouldRenderInOverlay() {
            return true;
        }
    }
}