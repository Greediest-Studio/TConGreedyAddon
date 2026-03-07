package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.network.NetworkHandler;
import com.smd.tcongreedyaddon.network.SwitchSpellPacket;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEventHandler {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            if (player == null) return;

            if (KeyBindings.leftpage.isPressed()) {
                ItemStack held = player.getHeldItemMainhand();
                if (held.getItem() instanceof MagicBook) {
                    int slot = 0;
                    NetworkHandler.INSTANCE.sendToServer(new SwitchSpellPacket(slot));

                }
            }
            if (KeyBindings.rightpage.isPressed()) {
                ItemStack held = player.getHeldItemMainhand();
                if (held.getItem() instanceof MagicBook) {
                    int slot = 1;
                    NetworkHandler.INSTANCE.sendToServer(new SwitchSpellPacket(slot));
                }
            }
        }
    }
}