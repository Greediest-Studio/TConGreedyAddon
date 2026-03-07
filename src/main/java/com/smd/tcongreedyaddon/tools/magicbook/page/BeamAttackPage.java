package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.List;

public class BeamAttackPage extends MultiSpellPage {

    public BeamAttackPage() {
        setTranslationKey("beam_attack_page").setRegistryName("beam_attack_page");
    }

    @Override
    protected void registerSpells() {
        addSpell(new Spell.Builder()
                .name("beam_attack")
                .cooldown(20)
                .icon(new ResourceLocation("minecraft", "items/large_fireball"))
                .action((world, player, toolStack, pageData) -> {
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
                })
        );
    }

    @Override
    public SlotType getSlotType() {
        return SlotType.RIGHT;
    }
}