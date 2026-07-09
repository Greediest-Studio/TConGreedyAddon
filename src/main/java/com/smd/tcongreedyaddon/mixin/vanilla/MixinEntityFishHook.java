package com.smd.tcongreedyaddon.mixin.vanilla;

import com.smd.tcongreedyaddon.tools.fishingrod.FishingRodHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityFishHook.class)
public abstract class MixinEntityFishHook {

    @Shadow
    private EntityPlayer angler;

    @Inject(method = "shouldStopFishing", at = @At("HEAD"), cancellable = true)
    private void tcongreedyaddon$allowTinkerFishingRod(CallbackInfoReturnable<Boolean> cir) {
        EntityFishHook hook = (EntityFishHook) (Object) this;
        EntityPlayer angler = hook.getAngler();

        if (angler == null) {
            return;
        }

        boolean hasRod = isUsableFishingRod(angler.getHeldItemMainhand())
                || isUsableFishingRod(angler.getHeldItemOffhand());

        if (!angler.isDead && angler.isEntityAlive() && hasRod && hook.getDistanceSq(angler) <= 1024.0D) {
            cir.setReturnValue(false);
        } else {
            hook.setDead();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void tcongreedyaddon$onHookedEntityTick(CallbackInfo ci) {
        EntityFishHook hook = (EntityFishHook) (Object) this;
        Entity target = hook.caughtEntity;

        if (hook.world.isRemote
                || hook.currentState != EntityFishHook.State.HOOKED_IN_ENTITY
                || target == null
                || target.isDead) {
            return;
        }

        EntityPlayer angler = hook.getAngler();
        if (angler == null || angler.fishEntity != hook) {
            return;
        }

        ItemStack rod = FishingRodHooks.findRod(angler);
        if (!rod.isEmpty()) {
            FishingRodHooks.onHookedEntityTick(rod, angler, hook, target);
        }
    }

    @Inject(method = "setHookedEntity", at = @At("HEAD"))
    private void tcongreedyaddon$recordHookHitSpeed(CallbackInfo ci) {
        EntityFishHook hook = (EntityFishHook) (Object) this;

        if (!hook.world.isRemote && hook.caughtEntity != null) {
            FishingRodHooks.recordHitSpeed(hook);
        }
    }

    @Inject(method = "canBeHooked", at = @At("HEAD"), cancellable = true)
    private void tcongreedyaddon$preventTinkerRodHookingSelf(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity == this.angler
                && this.angler != null
                && !FishingRodHooks.findRod(this.angler).isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    private static boolean isUsableFishingRod(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof ItemFishingRod || FishingRodHooks.isFishingRod(stack));
    }
}
