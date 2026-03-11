package com.smd.tcongreedyaddon.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.smd.tcongreedyaddon.Tags;

/**
 * Configuration for the Spell Overlay HUD.
 * All position values are fixed-pixel offsets from a screen edge (in scaled GUI pixels),
 * so the slots maintain their relative screen position when the window is resized.
 */
@Config(modid = Tags.MOD_ID, name = "TConGreedyAddon/SpellOverlay")
@Config.LangKey("tcongreedyaddon.config.spell_overlay")
public class SpellOverlayConfig {

    @Config.Comment({
        "X offset (in scaled GUI pixels) from the RIGHT edge of the screen",
        "for the active (selectable) spell slot group.",
        "Increase to move the group further from the right edge.",
        "Default: 33"
    })
    @Config.Name("Active Spells X Offset From Right")
    @Config.RangeInt(min = 0, max = 2000)
    public static int activeSpellsXOffsetFromRight = 33;

    @Config.Comment({
        "X offset (in scaled GUI pixels) from the LEFT edge of the screen",
        "for the passive (non-selectable) spell slot group.",
        "Increase to move the group further from the left edge.",
        "Default: 90"
    })
    @Config.Name("Passive Spells X Offset From Left")
    @Config.RangeInt(min = 0, max = 2000)
    public static int passiveSpellsXOffsetFromLeft = 90;

    @Config.Comment({
        "Y offset (in scaled GUI pixels) from the BOTTOM edge of the screen",
        "for all spell slots.",
        "Increase to move the slots further up from the bottom of the screen.",
        "Default: 1"
    })
    @Config.Name("Spells Y Offset From Bottom")
    @Config.RangeInt(min = 0, max = 2000)
    public static int spellsYOffsetFromBottom = 23;

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
