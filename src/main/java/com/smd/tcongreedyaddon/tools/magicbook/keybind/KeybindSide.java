package com.smd.tcongreedyaddon.tools.magicbook.keybind;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;

public enum KeybindSide {
    LEFT,
    RIGHT;

    public MagicPageItem.SlotType toSlotType() {
        return this == LEFT ? MagicPageItem.SlotType.LEFT : MagicPageItem.SlotType.RIGHT;
    }
}
