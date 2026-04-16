package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.network.KeybindInputPacket;
import com.smd.tcongreedyaddon.network.NetworkHandler;
import com.smd.tcongreedyaddon.network.SwitchSpellPacket;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindAction;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindChannel;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEventHandler {
    private boolean leftSkillAWasDown;
    private boolean leftSkillBWasDown;
    private boolean rightSkillAWasDown;
    private boolean rightSkillBWasDown;
    private int inputSequence;
    private int clientTick;

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

            clientTick++;
            handleKeyEdge(holdingBook, KeyBindings.leftSkillA, KeybindSide.LEFT, KeybindChannel.A);
            handleKeyEdge(holdingBook, KeyBindings.leftSkillB, KeybindSide.LEFT, KeybindChannel.B);
            handleKeyEdge(holdingBook, KeyBindings.rightSkillA, KeybindSide.RIGHT, KeybindChannel.A);
            handleKeyEdge(holdingBook, KeyBindings.rightSkillB, KeybindSide.RIGHT, KeybindChannel.B);
        }
    }

    private void handleKeyEdge(boolean holdingBook, KeyBinding keyBinding, KeybindSide side, KeybindChannel channel) {
        boolean isDown = keyBinding.isKeyDown();
        boolean wasDown = getWasDown(side, channel);
        if (holdingBook && isDown != wasDown) {
            KeybindAction action = isDown ? KeybindAction.PRESS : KeybindAction.RELEASE;
            NetworkHandler.INSTANCE.sendToServer(new KeybindInputPacket(
                    ++inputSequence,
                    side,
                    channel,
                    action,
                    clientTick
            ));
        }
        setWasDown(side, channel, isDown);
    }

    private boolean getWasDown(KeybindSide side, KeybindChannel channel) {
        if (side == KeybindSide.LEFT && channel == KeybindChannel.A) {
            return leftSkillAWasDown;
        }
        if (side == KeybindSide.LEFT) {
            return leftSkillBWasDown;
        }
        if (channel == KeybindChannel.A) {
            return rightSkillAWasDown;
        }
        return rightSkillBWasDown;
    }

    private void setWasDown(KeybindSide side, KeybindChannel channel, boolean value) {
        if (side == KeybindSide.LEFT && channel == KeybindChannel.A) {
            leftSkillAWasDown = value;
            return;
        }
        if (side == KeybindSide.LEFT) {
            leftSkillBWasDown = value;
            return;
        }
        if (channel == KeybindChannel.A) {
            rightSkillAWasDown = value;
            return;
        }
        rightSkillBWasDown = value;
    }
}
