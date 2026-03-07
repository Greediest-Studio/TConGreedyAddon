package com.smd.tcongreedyaddon.tools.magicbook.page;


import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.List;

public class BeamAttackPage extends MultiSpellPage {

    public BeamAttackPage() {
        setTranslationKey("beam_attack_page").setRegistryName("beam_attack_page");
    }

    @Override
    protected void registerSpells() {
        addSpell("Beam Attack", 20, (world, player, toolStack, pageData) -> {
            if (world.isRemote) return true;
            float range = MagicBook.getBeamRangeFromBook(toolStack);
            AxisAlignedBB aabb = player.getEntityBoundingBox().grow(range);
            List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                    entity -> entity != player && entity.isEntityAlive());

            float baseDamage = ToolHelper.getActualAttack(toolStack);
            for (EntityLivingBase target : targets) {
                target.attackEntityFrom(DamageSource.causePlayerDamage(player), baseDamage);
            }
            return true;
        });
    }

    @Override
    public SlotType getSlotType() {
        return SlotType.RIGHT; // 右键槽
    }
}