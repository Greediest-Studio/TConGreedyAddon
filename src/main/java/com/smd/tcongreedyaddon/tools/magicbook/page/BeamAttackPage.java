package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.List;

public class BeamAttackPage extends UnifiedMagicPage {

    public BeamAttackPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(new BeamAttackSpell())
                .displayName("beam_attack_page")
        );

        setRegistryName("beam_attack_page");
        setTranslationKey("beam_attack_page");
    }

    // 光束攻击法术
    private static class BeamAttackSpell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.RIGHT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.RIGHT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (context.world.isRemote) return true;
            float range = MagicBook.getBeamRangeFromBook(context.bookStack);
            AxisAlignedBB aabb = context.player.getEntityBoundingBox().grow(range);
            List<EntityLivingBase> targets = context.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                    entity -> entity != context.player && entity.isEntityAlive());

            float baseDamage = ToolHelper.getActualAttack(context.bookStack);
            for (EntityLivingBase target : targets) {
                target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), baseDamage);
            }
            return true;
        }

        @Override
        public boolean isSelectable() { return true; }

        @Override
        public String getNameKey() { return "beam_attack"; }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation("minecraft", "textures/items/fireball.png");
        }

        @Override
        public int getCooldownTicks() { return 20; }

        @Override
        public boolean shouldRenderInOverlay() { return true; }
    }
}