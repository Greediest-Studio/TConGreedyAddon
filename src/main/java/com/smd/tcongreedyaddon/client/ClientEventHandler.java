package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.network.NetworkHandler;
import com.smd.tcongreedyaddon.network.SwitchSpellPacket;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
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
                    NetworkHandler.INSTANCE.sendToServer(new SwitchSpellPacket(0, true));
                }
            }
            if (KeyBindings.rightpage.isPressed()) {
                ItemStack held = player.getHeldItemMainhand();
                if (held.getItem() instanceof MagicBook) {
                    NetworkHandler.INSTANCE.sendToServer(new SwitchSpellPacket(1, true));
                }
            }
            // 如需切换上一个，可定义新的按键并发送 next=false 的包
        }
    }
}