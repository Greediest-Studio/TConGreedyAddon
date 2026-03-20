package com.smd.tcongreedyaddon.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    private static final String KEY_CATEGORY = "key.tcongreedyaddon";

    public static final KeyBinding leftpage = new KeyBinding(
            "key.tcongreedyaddon.leftpage",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_NONE,
            KEY_CATEGORY
    );

    public static final KeyBinding rightpage = new KeyBinding(
            "key.tcongreedyaddon.rightpage",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_NONE,
            KEY_CATEGORY
    );

    public static final KeyBinding utilitySkill = new KeyBinding(
            "key.tcongreedyaddon.utilityskill",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_Q,
            KEY_CATEGORY
    );

    public static final KeyBinding grappleMelee = new KeyBinding(
            "key.tcongreedyaddon.grapplemelee",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_R,
            KEY_CATEGORY
    );

}
