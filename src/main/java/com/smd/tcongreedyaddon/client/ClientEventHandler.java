package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.network.NetworkHandler;
import com.smd.tcongreedyaddon.network.GrappleMeleePacket;
import com.smd.tcongreedyaddon.network.SkillKeyPacket;
import com.smd.tcongreedyaddon.network.SwitchSpellPacket;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEventHandler {
    private boolean utilitySkillWasDown;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            if (player == null) return;

            ItemStack heldMain = player.getHeldItemMainhand();
            boolean holdingBook = heldMain.getItem() instanceof MagicBook;
            if (holdingBook) {
                if (player.isHandActive()
                        && player.getActiveHand() == net.minecraft.util.EnumHand.MAIN_HAND
                        && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    player.stopActiveHand();
                    MagicBook.clearClientHoldState(player);
                }
            } else {
                MagicBook.clearClientHoldState(player);
            }

            if (KeyBindings.leftpage.isPressed()) {
                ItemStack held = player.getHeldItemMainhand();
                if (held.getItem() instanceof MagicBook) {
                    NetworkHandler.INSTANCE.sendToServer(new SwitchSpellPacket(0, true));
                }
            }
            if (KeyBindings.rightpage.isPressed()) {
                ItemStack held = player.getHeldItemMainhand();
                if (held.getItem() instanceof MagicBook) {
                    NetworkHandler.INSTANCE.sendToServer(new SwitchSpellPacket(1, true));
                }
            }

            boolean skillDown = KeyBindings.utilitySkill.isKeyDown();
            if (holdingBook) {
                if (skillDown && !utilitySkillWasDown) {
                    NetworkHandler.INSTANCE.sendToServer(new SkillKeyPacket(SkillKeyPacket.Action.PRESS));
                }
                if (!skillDown && utilitySkillWasDown) {
                    NetworkHandler.INSTANCE.sendToServer(new SkillKeyPacket(SkillKeyPacket.Action.RELEASE));
                }
                if (KeyBindings.grappleMelee.isPressed()) {
                    NetworkHandler.INSTANCE.sendToServer(new GrappleMeleePacket());
                }
            }
            utilitySkillWasDown = skillDown;
        }
    }
}
