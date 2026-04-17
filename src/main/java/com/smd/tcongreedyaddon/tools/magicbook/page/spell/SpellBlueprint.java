package com.smd.tcongreedyaddon.tools.magicbook.page.spell;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SpellBlueprint {

    private final String nameKey;
    private final ResourceLocation icon;
    private final boolean selectable;
    private final boolean renderInOverlay;
    private final int cooldownTicks;
    private final int castActionTicks;
    private final List<Class<? extends Event>> listeningEvents;

    private SpellBlueprint(String nameKey, ResourceLocation icon, boolean selectable,
                           boolean renderInOverlay, int cooldownTicks, int castActionTicks,
                           List<Class<? extends Event>> listeningEvents) {
        this.nameKey = nameKey;
        this.icon = icon;
        this.selectable = selectable;
        this.renderInOverlay = renderInOverlay;
        this.cooldownTicks = cooldownTicks;
        this.castActionTicks = castActionTicks;
        this.listeningEvents = Collections.unmodifiableList(new ArrayList<>(listeningEvents));
    }

    public static Builder builder(String nameKey) {
        return new Builder(nameKey);
    }

    public String getNameKey() {
        return nameKey;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean shouldRenderInOverlay() {
        return renderInOverlay;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public int getCastActionTicks() {
        return castActionTicks;
    }

    public List<Class<? extends Event>> getListeningEvents() {
        return listeningEvents;
    }

    public static final class Builder {
        private final String nameKey;
        private ResourceLocation icon;
        private boolean selectable = true;
        private boolean renderInOverlay = true;
        private int cooldownTicks = 0;
        private int castActionTicks = 0;
        private final List<Class<? extends Event>> listeningEvents = new ArrayList<>();

        private Builder(String nameKey) {
            if (nameKey == null || nameKey.trim().isEmpty()) {
                throw new IllegalArgumentException("nameKey must not be empty");
            }
            this.nameKey = nameKey;
        }

        public Builder icon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public Builder selectable(boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        public Builder renderInOverlay(boolean renderInOverlay) {
            this.renderInOverlay = renderInOverlay;
            return this;
        }

        public Builder cooldown(int ticks) {
            if (ticks < 0) {
                throw new IllegalArgumentException("cooldown must be >= 0");
            }
            this.cooldownTicks = ticks;
            return this;
        }

        public Builder castActionTicks(int ticks) {
            if (ticks < 0) {
                throw new IllegalArgumentException("castActionTicks must be >= 0");
            }
            this.castActionTicks = ticks;
            return this;
        }

        public Builder listeningEvents(Class<? extends Event>... events) {
            if (events != null) {
                this.listeningEvents.addAll(Arrays.asList(events));
            }
            return this;
        }

        public Builder listeningEvents(List<Class<? extends Event>> events) {
            if (events != null) {
                this.listeningEvents.addAll(events);
            }
            return this;
        }

        public SpellBlueprint build() {
            return new SpellBlueprint(nameKey, icon, selectable, renderInOverlay, cooldownTicks, castActionTicks, listeningEvents);
        }
    }
}
