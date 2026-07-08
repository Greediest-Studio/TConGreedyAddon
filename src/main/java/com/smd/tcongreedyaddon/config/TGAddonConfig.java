package com.smd.tcongreedyaddon.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import com.smd.tcongreedyaddon.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_NAME)
public class TGAddonConfig {

    static {
        ConfigAnytime.register(TGAddonConfig.class);
    }
}
