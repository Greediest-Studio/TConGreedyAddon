package com.smd.tcongreedyaddon.plugin;

import com.smd.tcongreedyaddon.plugin.abyssalcraft.abyssalcraft;
import com.smd.tcongreedyaddon.plugin.oldweapons.OldWeapons;
import com.smd.tcongreedyaddon.plugin.solidarytinker.solidarytinker;
import com.smd.tcongreedyaddon.plugin.something.something;

public final class Modules {

    private Modules() {
    }

    public static void registerAll(ModuleManager manager) {
        manager.registerModule(new OldWeapons());
        manager.registerModule(new abyssalcraft());
        manager.registerModule(new something());
        manager.registerModule(new solidarytinker());
    }
}
