package com.smd.tcongreedyaddon.tools.magicbook.page;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

public class DefaultAttackPage extends UnifiedMagicPage {

    public DefaultAttackPage() {
        super(new UnifiedMagicPage.Builder(SlotType.LEFT)
                .addLeftSpell(new LeftSpell.Builder()
                        .name("default_attack")
                        .cooldown(0)
                        .action(DefaultAttackPage::performStandardAttack))
                .displayName("default_attack_page")
        );

        setRegistryName("default_attack_page");
        setTranslationKey("default_attack_page");
    }

    private static boolean performStandardAttack(ItemStack toolStack, EntityPlayer player, Entity target) {
        if (toolStack.getItem() instanceof TinkerToolCore) {
            return ToolHelper.attackEntity(toolStack, (TinkerToolCore) toolStack.getItem(), player, target);
        }
        return false;
    }
}