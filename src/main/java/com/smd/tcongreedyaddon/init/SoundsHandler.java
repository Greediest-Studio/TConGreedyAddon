package com.smd.tcongreedyaddon.init;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class SoundsHandler {
    public static SoundEvent CIALLO;
    public static SoundEvent SUCAI_1;
    public static SoundEvent SUCAI_2;
    public static SoundEvent SUCAI_3;
    public static SoundEvent SUCAI_4;
    public static SoundEvent SUCAI_5;
    public static SoundEvent SUCAI_6;
    public static SoundEvent SUCAI_7;
    public static void register(IForgeRegistry<SoundEvent> registry) {
        CIALLO = registerSound(registry, "player.ciallo");
        SUCAI_1 = registerSound(registry, "player.sucai_1");
        SUCAI_2 = registerSound(registry, "player.sucai_2");
        SUCAI_3 = registerSound(registry, "player.sucai_3");
        SUCAI_4 = registerSound(registry, "player.sucai_4");
        SUCAI_5 = registerSound(registry, "player.sucai_5");
        SUCAI_6 = registerSound(registry, "player.sucai_6");
        SUCAI_7 = registerSound(registry, "player.sucai_7");
    }

    public static SoundEvent registerSound(IForgeRegistry<SoundEvent> registry, String soundName) {
        ResourceLocation soundID = new ResourceLocation(TConGreedyAddon.MOD_ID, soundName);
        SoundEvent event = new SoundEvent(soundID).setRegistryName(soundID);
        registry.register(event);
        return event;
    }
}
