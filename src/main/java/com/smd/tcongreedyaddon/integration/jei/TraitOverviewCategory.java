package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.shared.TinkerCommons;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TraitOverviewCategory implements IRecipeCategory<TraitOverviewWrapper> {

    static final String UID = "tcongreedyaddon:trait_overview";
    private static final int WIDTH = 178;
    private static final int HEIGHT = 150;
    private static final int TOOL_SLOT = 0;
    private static final int CONTENT_LEFT = 12;
    private static final int CONTENT_WIDTH = WIDTH - (CONTENT_LEFT * 2);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    private TraitOverviewWrapper currentRecipe;

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
        IFocus<?> focus = recipeLayout.getFocus();
        if (focus != null && focus.getValue() instanceof ItemStack) {
            recipeWrapper.updateFocus((ItemStack) focus.getValue());
        } else {
            recipeWrapper.updateFocus(ItemStack.EMPTY);
        }

        this.currentRecipe = recipeWrapper;
        recipeLayout.getItemStacks().init(TOOL_SLOT, true, 0, 0);
        recipeLayout.getItemStacks().set(TOOL_SLOT, Collections.singletonList(recipeWrapper.getDisplayStack()));
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        if (currentRecipe == null) {
            return;
        }

        FontRenderer fontRenderer = minecraft.fontRenderer;
        slot.draw(minecraft, 0, 0);

        String toolName = currentRecipe.getDisplayStack().getDisplayName();
        drawCenteredSplit(fontRenderer, toolName, 6, CONTENT_WIDTH, 0xE0E0E0);

        String pageText = I18n.format("gui.jei.trait_overview.page", currentRecipe.getCurrentPage(), Math.max(1, currentRecipe.getPageCount()));
        fontRenderer.drawStringWithShadow(pageText, WIDTH - fontRenderer.getStringWidth(pageText), 2, 0xA0A0A0);

        ITrait trait = currentRecipe.getCurrentTrait();
        if (trait == null) {
            fontRenderer.drawSplitString(I18n.format("gui.jei.trait_overview.empty"), CONTENT_LEFT, 34, CONTENT_WIDTH, 0xC8C8C8);
            return;
        }

        drawCentered(fontRenderer, TextFormatting.UNDERLINE + trait.getLocalizedName() + TextFormatting.RESET, 26, 0xFFF0F0F0, true);
        String descriptionKey = String.format("tcongreedyaddon.jei.modifier.%s.text", trait.getIdentifier());
        fontRenderer.drawSplitString(I18n.format(descriptionKey), CONTENT_LEFT, 42, CONTENT_WIDTH, 0xD8D8D8);
    }

    @Nonnull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (currentRecipe == null) {
            return Collections.emptyList();
        }

        List<String> tooltip = new ArrayList<>();
        if (mouseX >= 0 && mouseX < 18 && mouseY >= 0 && mouseY < 18) {
            tooltip.add(currentRecipe.getDisplayStack().getDisplayName());
        }
        return tooltip;
    }

    private void drawCentered(FontRenderer fontRenderer, String text, int y, int color, boolean shadow) {
        fontRenderer.drawString(text, (WIDTH - fontRenderer.getStringWidth(text)) / 2f, y, color, shadow);
    }

    private void drawCenteredSplit(FontRenderer fontRenderer, String text, int y, int maxWidth, int color) {
        List<String> lines = fontRenderer.listFormattedStringToWidth(text, maxWidth);
        int lineY = y;
        for (String line : lines) {
            fontRenderer.drawString(line, (WIDTH - fontRenderer.getStringWidth(line)) / 2f, lineY, color, false);
            lineY += fontRenderer.FONT_HEIGHT;
        }
    }
}