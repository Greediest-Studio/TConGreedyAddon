package com.smd.tcongreedyaddon.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.smd.tcongreedyaddon.Tags;

/**
 * Configuration for TConGreedyAddon's Material Shader Fix.
 * Allows users to control behavior of the Mixin that fixes rendering for custom part types.
 */
@Config(modid = Tags.MOD_ID, name = "TConGreedyAddon/MaterialShaderFix")
@Config.LangKey("tcongreedyaddon.config.shader_fix")
public class MaterialShaderFixConfig {
    
    @Config.Comment({
        "Enable the material shader fix for custom part types (laser_medium, battery_cell, tconevo.magic).",
        "When enabled, materials with standard stats will render correctly on custom parts,",
        "even if they don't have the custom stat types.",
        "Disable this if you experience compatibility issues.",
        "Note: Changing this requires a game restart to take effect."
    })
    @Config.Name("Enable Shader Fix")
    @Config.RequiresMcRestart
    public static boolean enableShaderFix = true;
    
    @Config.Comment({
        "Enable detailed logging of which materials benefit from the shader fix.",
        "Useful for debugging but can spam the log during startup."
    })
    @Config.Name("Enable Debug Logging")
    @Config.RequiresMcRestart
    public static boolean enableDebugLogging = true;
    
    @Config.Comment({
        "Custom stat types to apply the shader fix for.",
        "Default includes PlusTiC and Tinkers-Evolution stat types.",
        "Add your own custom stat types here if needed."
    })
    @Config.Name("Custom Stat Types")
    @Config.RequiresMcRestart
    public static String[] customStatTypes = {
        "laser_medium",
        "battery_cell",
        "tconevo.magic"
    };
    
    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
