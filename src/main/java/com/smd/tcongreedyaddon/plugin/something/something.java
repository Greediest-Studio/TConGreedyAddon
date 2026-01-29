package com.smd.tcongreedyaddon.plugin.something;

import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.traits.something.TraitCiallo;
import com.smd.tcongreedyaddon.traits.something.TraitSoundEffect;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class something implements IModule {

    @Override
    public String getModuleName() { return "something"; }

    public static final AbstractTrait ciallo = new TraitCiallo();
    public static final AbstractTrait soundeffects = new TraitSoundEffect();

    @Override
    public boolean isModAvailable() {
        return Loader.isModLoaded("tconstruct");
    }

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
        TinkerRegistry.addTrait(ciallo);
        TinkerRegistry.addTrait(soundeffects);
    }

    @Override
    public void postInit() {
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
    }
}
