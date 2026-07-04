package com.smd.tcongreedyaddon.plugin;

import com.smd.tcongreedyaddon.plugin.abyssalcraft.abyssalcraft;
import com.smd.tcongreedyaddon.plugin.oldweapons.OldWeapons;
import com.smd.tcongreedyaddon.plugin.solidarytinker.solidarytinker;
import com.smd.tcongreedyaddon.plugin.something.something;
import net.minecraftforge.fml.common.Loader;

public final class Modules {

    private Modules() {
    }

    public static void registerAll(ModuleManager manager) {
        if (Loader.isModLoaded("tconstruct")) {
            manager.registerModule(new OldWeapons());
            manager.registerModule(new something());
            manager.registerModule(new solidarytinker());

            if (Loader.isModLoaded("abyssalcraft")) {
                manager.registerModule(new abyssalcraft());
            }
        }
    }
}
