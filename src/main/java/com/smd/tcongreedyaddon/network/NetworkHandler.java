package com.smd.tcongreedyaddon.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("tc_magicbook");
    private static int id = 0;

    public static void register() {
        INSTANCE.registerMessage(SwitchSpellPacketHandler.class, SwitchSpellPacket.class, id++, Side.SERVER);
    }
}