package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.Tags;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class FireballPage extends MultiSpellPage {

    public FireballPage() {
        setTranslationKey("fireball_page").setRegistryName("fireball_page");
    }

    @Override
    protected void registerSpells() {
        // 小火球
        addSpell(new Spell.Builder()
                .name("small_fire_ball")
                .cooldown(20)
                .icon(new ResourceLocation(Tags.MOD_ID, "textures/item/gaiaspawn.png"))
                .action((world, player, toolStack, pageData) -> {
                    if (world.isRemote) return true;
                    Vec3d look = player.getLookVec();
                    EntitySmallFireball fireball = new EntitySmallFireball(world, player, 1, 1, 1);
                    fireball.setPosition(
                            player.posX + look.x * 1.5,
                            player.posY + look.y * 1.5 + player.getEyeHeight(),
                            player.posZ + look.z * 1.5
                    );
                    fireball.accelerationX = look.x * 0.3;
                    fireball.accelerationY = look.y * 0.3;
                    fireball.accelerationZ = look.z * 0.3;
                    world.spawnEntity(fireball);
                    player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
                    return true;
                })
        );

        // 大火球
        addSpell(new Spell.Builder()
                .name("large_fire_ball")
                .cooldown(60)
                .icon(new ResourceLocation("minecraft", "item/large_fireball"))
                .action((world, player, toolStack, pageData) -> {
                    if (world.isRemote) return true;
                    Vec3d look = player.getLookVec();
                    EntityLargeFireball fireball = new EntityLargeFireball(world, player, look.x, look.y, look.z);
                    fireball.setPosition(
                            player.posX + look.x * 1.5,
                            player.posY + look.y * 1.5 + player.getEyeHeight(),
                            player.posZ + look.z * 1.5
                    );
                    fireball.accelerationX = look.x * 0.1;
                    fireball.accelerationY = look.y * 0.1;
                    fireball.accelerationZ = look.z * 0.1;
                    world.spawnEntity(fireball);
                    player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
                    return true;
                })
        );
    }

    @Override
    public SlotType getSlotType() {
        return SlotType.RIGHT;
    }
}