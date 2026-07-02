package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import slimeknights.tconstruct.shared.TinkerCommons;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TraitOverviewCategory implements IRecipeCategory<TraitOverviewWrapper> {

    static final String UID = "tcongreedyaddon:trait_overview";
    private static final int WIDTH = 178;
    private static final int HEIGHT = 166;
    private static final int TOOL_SLOT = 0;
    private static final int TOOL_SLOT_X = 0;
    private static final int TOOL_SLOT_Y = 0;
    private static final int CONTENT_LEFT = 6;
    private static final int CONTENT_TOP = 24;
    private static final int CONTENT_WIDTH = WIDTH - (CONTENT_LEFT * 2);
    private static final int CONTENT_BOTTOM = HEIGHT - 4;
    private static final int LINE_SPACING = 1;
    private static final int SECTION_SPACING = 3;
    private static final int SCROLL_STEP = 18;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    private TraitOverviewWrapper currentRecipe;
    private int scrollOffset;
    private int maxScrollOffset;
    private final List<EntryBounds> entryBounds = new ArrayList<>();

    TraitOverviewCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(TinkerCommons.book));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format("gui.jei.trait_overview.title");
    }

    @Nonnull
    @Override
    public String getModName() {
        return "TConGreedyAddon";
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull TraitOverviewWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        this.currentRecipe = recipeWrapper;
        this.scrollOffset = 0;
        this.maxScrollOffset = 0;
        this.entryBounds.clear();

        recipeLayout.getItemStacks().init(TOOL_SLOT, true, TOOL_SLOT_X, TOOL_SLOT_Y);
        if (!recipeWrapper.getDisplayStack().isEmpty()) {
            recipeLayout.getItemStacks().set(TOOL_SLOT, Collections.singletonList(recipeWrapper.getDisplayStack()));
        }
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        if (currentRecipe == null) {
            return;
        }

        FontRenderer fontRenderer = minecraft.fontRenderer;
        if (!currentRecipe.getDisplayStack().isEmpty()) {
            slot.draw(minecraft, TOOL_SLOT_X, TOOL_SLOT_Y);
            String toolName = currentRecipe.getDisplayStack().getDisplayName();
            drawHeader(fontRenderer, toolName);
        }

        drawScrollHint(fontRenderer);

        if (currentRecipe.isEmptyState()) {
            fontRenderer.drawSplitString(I18n.format("gui.jei.trait_overview.empty_focus"), CONTENT_LEFT, CONTENT_TOP + 8, CONTENT_WIDTH, 0xC8C8C8);
            return;
        }

        List<RenderableLine> lines = buildRenderableLines(fontRenderer, currentRecipe.getEntries());
        int totalHeight = lines.isEmpty() ? 0 : lines.get(lines.size() - 1).bottom();
        maxScrollOffset = Math.max(0, totalHeight - (CONTENT_BOTTOM - CONTENT_TOP));
        updateScrollOffsetFromMouse();

        for (RenderableLine line : lines) {
            int drawY = CONTENT_TOP + line.relativeY - scrollOffset;
            if (drawY + fontRenderer.FONT_HEIGHT <= CONTENT_TOP || drawY >= CONTENT_BOTTOM) {
                continue;
            }
            fontRenderer.drawString(line.text, line.x, drawY, line.color, false);
        }
    }

    @Nonnull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (currentRecipe == null || currentRecipe.getDisplayStack().isEmpty()) {
            return Collections.emptyList();
        }

        if (mouseX >= TOOL_SLOT_X && mouseX < TOOL_SLOT_X + 18 && mouseY >= TOOL_SLOT_Y && mouseY < TOOL_SLOT_Y + 18) {
            return Collections.singletonList(currentRecipe.getDisplayStack().getDisplayName());
        }

        TraitOverviewEntry hoveredEntry = getHoveredEntry(mouseX, mouseY);
        if (hoveredEntry == null) {
            return Collections.emptyList();
        }

        List<String> tooltip = new ArrayList<>();
        tooltip.add(hoveredEntry.getDisplayName());
        if (GuiScreen.isShiftKeyDown()) {
            String localized = localizeOrFallback(hoveredEntry.getJeiDescriptionKey());
            tooltip.addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(localized, 220));
            tooltip.add(I18n.format("gui.jei.trait_overview.source", hoveredEntry.getSourceModId()));
        } else {
            String localized = localizeOrFallback(hoveredEntry.getDescriptionKey());
            tooltip.addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(localized, 220));
        }

        return tooltip;
    }

    private void updateScrollOffsetFromMouse() {
        int wheel = Mouse.getDWheel();
        if (wheel > 0) {
            scrollOffset = Math.max(0, scrollOffset - SCROLL_STEP);
        } else if (wheel < 0) {
            scrollOffset = Math.min(maxScrollOffset, scrollOffset + SCROLL_STEP);
        }
    }

    private void drawScrollHint(FontRenderer fontRenderer) {
        if (maxScrollOffset <= 0) {
            return;
        }

        String hint = I18n.format("gui.jei.trait_overview.scroll");
        fontRenderer.drawStringWithShadow(hint, WIDTH - fontRenderer.getStringWidth(hint), HEIGHT - fontRenderer.FONT_HEIGHT, 0xA0A0A0);
    }

    private List<RenderableLine> buildRenderableLines(FontRenderer fontRenderer, List<TraitOverviewEntry> entries) {
        if (entries.isEmpty()) {
            return Collections.singletonList(new RenderableLine(
                I18n.format("gui.jei.trait_overview.empty"),
                CONTENT_LEFT,
                8,
                0xC8C8C8
            ));
        }

        List<RenderableLine> lines = new ArrayList<>();
        int relativeY = 0;
        for (TraitOverviewEntry entry : entries) {
            int entryStart = relativeY;
            lines.add(new RenderableLine(entry.getDisplayName(), CONTENT_LEFT, relativeY, 0xFFF0F0F0));
            relativeY += fontRenderer.FONT_HEIGHT + LINE_SPACING;

            relativeY = appendWrappedLines(fontRenderer, lines, localizeOrFallback(entry.getDescriptionKey()), CONTENT_LEFT + 4, relativeY, 0xD8D8D8);
            entryBounds.add(new EntryBounds(entry, entryStart, relativeY));
            relativeY += SECTION_SPACING;
        }

        return lines;
    }

    private String localizeOrFallback(String key) {
        String localized = I18n.format(key);
        return localized.equals(key) ? key : localized;
    }

    private int appendWrappedLines(FontRenderer fontRenderer, List<RenderableLine> lines, String text, int x, int relativeY, int color) {
        List<String> wrapped = fontRenderer.listFormattedStringToWidth(text, CONTENT_WIDTH - (x - CONTENT_LEFT));
        for (String line : wrapped) {
            lines.add(new RenderableLine(line, x, relativeY, color));
            relativeY += fontRenderer.FONT_HEIGHT + LINE_SPACING;
        }
        return relativeY;
    }

    private void drawHeader(FontRenderer fontRenderer, String toolName) {
        List<String> lines = fontRenderer.listFormattedStringToWidth(toolName, WIDTH - 30);
        int lineY = 4;
        for (String line : lines) {
            fontRenderer.drawString(line, 22, lineY, 0xE0E0E0, false);
            lineY += fontRenderer.FONT_HEIGHT;
        }
    }

    private TraitOverviewEntry getHoveredEntry(int mouseX, int mouseY) {
        int relativeMouseY = mouseY - CONTENT_TOP + scrollOffset;
        for (EntryBounds bounds : entryBounds) {
            if (mouseX >= CONTENT_LEFT && mouseX <= CONTENT_LEFT + CONTENT_WIDTH
                && relativeMouseY >= bounds.startY && relativeMouseY <= bounds.endY) {
                return bounds.entry;
            }
        }
        return null;
    }

    private static class RenderableLine {
        private final String text;
        private final int x;
        private final int relativeY;
        private final int color;

        private RenderableLine(String text, int x, int relativeY, int color) {
            this.text = text;
            this.x = x;
            this.relativeY = relativeY;
            this.color = color;
        }

        private int bottom() {
            return relativeY + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        }
    }

    private static class EntryBounds {
        private final TraitOverviewEntry entry;
        private final int startY;
        private final int endY;

        private EntryBounds(TraitOverviewEntry entry, int startY, int endY) {
            this.entry = entry;
            this.startY = startY;
            this.endY = endY;
        }
    }
}
