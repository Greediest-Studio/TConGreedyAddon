package com.smd.tcongreedyaddon.init;

import slimeknights.tconstruct.library.modifiers.ModifierTrait;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TraitRegistry {

    public static final List<ModifierTrait> REGISTERED_TRAITS = new ArrayList<>();

    public static <T extends ModifierTrait> T register(T trait) {
        if (trait != null && !REGISTERED_TRAITS.contains(trait)) {
            REGISTERED_TRAITS.add(trait);
        }
        return trait;
    }

    public static Stream<ModifierTrait> stream() {
        return REGISTERED_TRAITS.stream();
    }
}

