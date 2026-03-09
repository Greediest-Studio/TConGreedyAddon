package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class FireballPage extends UnifiedMagicPage {

    public FireballPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(new SmallFireballSpell())   // 小火球
                .addRightSpell(new LargeFireballSpell())   // 大火球
                .displayName("fireball_page")
        );
        setRegistryName("fireball_page");
        setTranslationKey("fireball_page");
    }

    // 小火球法术（实现 ISpell 接口）
    private static class SmallFireballSpell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            // 只在右键点击时触发，且必须在右槽
            return context.trigger.isType(TriggerSource.Type.RIGHT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.RIGHT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (context.world.isRemote) return true;
            Vec3d look = context.player.getLookVec();
            EntitySmallFireball fireball = new EntitySmallFireball(context.world, context.player, 1, 1, 1);
            fireball.setPosition(
                    context.player.posX + look.x * 1.5,
                    context.player.posY + look.y * 1.5 + context.player.getEyeHeight(),
                    context.player.posZ + look.z * 1.5
            );
            fireball.accelerationX = look.x * 0.3;
            fireball.accelerationY = look.y * 0.3;
            fireball.accelerationZ = look.z * 0.3;
            context.world.spawnEntity(fireball);
            context.player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
            return true;
        }

        @Override
        public boolean isSelectable() {
            return true; // 参与索引切换
        }

        @Override
        public String getNameKey() {
            return "spell.small_fire_ball";
        }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/burning_dash.png");
        }

        @Override
        public int getCooldownTicks() {
            return 20;
        }
    }

    // 大火球法术（实现 ISpell 接口）
    private static class LargeFireballSpell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.RIGHT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.RIGHT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (context.world.isRemote) return true;
            Vec3d look = context.player.getLookVec();
            EntityLargeFireball fireball = new EntityLargeFireball(context.world, context.player, look.x, look.y, look.z);
            fireball.setPosition(
                    context.player.posX + look.x * 1.5,
                    context.player.posY + look.y * 1.5 + context.player.getEyeHeight(),
                    context.player.posZ + look.z * 1.5
            );
            fireball.accelerationX = look.x * 0.1;
            fireball.accelerationY = look.y * 0.1;
            fireball.accelerationZ = look.z * 0.1;
            context.world.spawnEntity(fireball);
            context.player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
            return true;
        }

        @Override
        public boolean isSelectable() {
            return true;
        }

        @Override
        public String getNameKey() {
            return "spell.large_fire_ball";
        }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/charge.png");
        }

        @Override
        public int getCooldownTicks() {
            return 60;
        }
    }
}