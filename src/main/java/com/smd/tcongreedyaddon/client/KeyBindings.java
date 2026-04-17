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

    public static final KeyBinding leftSkillA = new KeyBinding(
            "key.tcongreedyaddon.leftskilla",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_Q,
            KEY_CATEGORY
    );

    public static final KeyBinding leftSkillB = new KeyBinding(
            "key.tcongreedyaddon.leftskillb",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_R,
            KEY_CATEGORY
    );

    public static final KeyBinding rightSkillA = new KeyBinding(
            "key.tcongreedyaddon.rightskilla",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_Z,
            KEY_CATEGORY
    );

    public static final KeyBinding rightSkillB = new KeyBinding(
            "key.tcongreedyaddon.rightskillb",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_X,
            KEY_CATEGORY
    );

}
