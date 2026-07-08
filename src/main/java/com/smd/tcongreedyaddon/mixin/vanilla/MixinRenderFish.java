package com.smd.tcongreedyaddon.mixin.vanilla;

import com.smd.tcongreedyaddon.tools.fishingrod.FishingRodHooks;
import net.minecraft.client.renderer.entity.RenderFish;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderFish.class)
public abstract class MixinRenderFish {

    @Redirect(
            method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;")
    )
    private Item tcongreedyaddon$treatTinkerRodAsFishingRod(ItemStack stack) {
        return FishingRodHooks.isFishingRod(stack) ? Items.FISHING_ROD : stack.getItem();
    }
}
