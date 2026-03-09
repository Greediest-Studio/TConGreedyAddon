package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.LeftClickSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.PassiveSpell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

public class DefaultAttackPage extends UnifiedMagicPage {

    public DefaultAttackPage() {
        super(new UnifiedMagicPage.Builder(SlotType.LEFT)

                .addLeftSpell(new LeftClickSpell.Builder()
                        .name("default_attack")
                        .cooldown(0)
                        .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/3.png"))
                        .action(DefaultAttackPage::performStandardAttack)
                        .build())

                .addLeftSpell(new LeftClickSpell.Builder()
                        .name("test_attack_1")
                        .cooldown(10)
                        .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/1.png"))
                        .action((toolStack, player, target) -> {
                            if (!player.world.isRemote) {
                                target.attackEntityFrom(DamageSource.causePlayerDamage(player), 1.0f);
                                player.sendMessage(new TextComponentString("Test attack 1 used!"));
                                target.setFire(60);
                            }
                            return true;
                        })
                        .build())

                .addLeftSpell(new LeftClickSpell.Builder()
                        .name("test_attack_2")
                        .cooldown(40)
                        .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/4.png"))
                        .action((toolStack, player, target) -> {
                            if (!player.world.isRemote) {
                                target.attackEntityFrom(DamageSource.causePlayerDamage(player), 3.0f);
                                player.sendMessage(new TextComponentString("Test attack 2 used!"));
                            }
                            return true;
                        })
                        .build())

                .addLeftSpell(new LeftClickSpell.Builder()
                        .name("test_attack_3")
                        .cooldown(10)
                        .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/2.png"))
                        .action((toolStack, player, target) -> {
                            if (!player.world.isRemote) {
                                target.attackEntityFrom(DamageSource.causePlayerDamage(player), 1.0f);
                                player.sendMessage(new TextComponentString("Test attack 1 used!"));
                                target.setFire(60);
                            }
                            return true;
                        })
                        .build())

                .addLeftSpell(new LeftClickSpell.Builder()
                        .name("test_attack_4")
                        .cooldown(40)
                        .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/5.png"))
                        .action((toolStack, player, target) -> {
                            if (!player.world.isRemote) {
                                target.attackEntityFrom(DamageSource.causePlayerDamage(player), 3.0f);
                                player.sendMessage(new TextComponentString("Test attack 2 used!"));
                            }
                            return true;
                        })
                        .build())

                .addLeftSpell(new LeftClickSpell.Builder()
                        .name("test_attack_5")
                        .cooldown(60)
                        .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/6.png"))
                        .action((toolStack, player, target) -> {
                            if (!player.world.isRemote) {
                                target.attackEntityFrom(DamageSource.causePlayerDamage(player), 5.0f);
                                player.sendMessage(new TextComponentString("Test attack 3 used!"));
                                target.setFire(100);
                            }
                            return true;
                        })
                        .build())

                .addPassive(new PassiveSpell.Builder()
                        .name("spell.6666")
                        .interval(40)
                        .action((world, player, toolStack, pageData) -> {
                            if (!world.isRemote) {
                                player.sendMessage(new TextComponentString("Passive effect triggered!"));
                            }
                        })
                        .runOnClient(false)
                        .build())
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