package com.smd.tcongreedyaddon.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unchecked")
public final class TicArmorTraitCache {

    public static final TicArmorTraitCache INSTANCE = new TicArmorTraitCache();

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = {
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    };

    private final Map<UUID, ArmorTraitSnapshot> cache = new HashMap<>();

    private TicArmorTraitCache() {
    }

    public String[] getArmorTraits(EntityPlayer player) {
        return getSnapshot(player).copyAllTraits();
    }

    public String[] getArmorSlotTraits(EntityPlayer player, String slotName) {
        EntityEquipmentSlot slot = TicToolStacks.parseArmorSlot(slotName);
        return getArmorSlotTraits(player, slot);
    }

    public String[] getArmorSlotTraits(EntityPlayer player, EntityEquipmentSlot slot) {
        int index = TicToolStacks.armorSlotIndex(slot);
        if (index < 0) {
            return TicToolNbt.EMPTY_STRINGS;
        }
        return getSnapshot(player).copySlotTraits(index);
    }

    public boolean hasArmorTrait(EntityPlayer player, String traitId) {
        if (traitId == null || traitId.trim().isEmpty()) {
            return false;
        }
        return getSnapshot(player).allTraitSet.contains(traitId);
    }

    public boolean hasArmorSlotTrait(EntityPlayer player, String slotName, String traitId) {
        if (traitId == null || traitId.trim().isEmpty()) {
            return false;
        }
        EntityEquipmentSlot slot = TicToolStacks.parseArmorSlot(slotName);
        int index = TicToolStacks.armorSlotIndex(slot);
        if (index < 0) {
            return false;
        }
        return getSnapshot(player).slotTraitSets[index].contains(traitId);
    }

    public boolean refresh(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        cache.put(player.getUniqueID(), buildSnapshot(player));
        return true;
    }

    public void clear(EntityPlayer player) {
        if (player != null) {
            cache.remove(player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        if (TicToolStacks.armorSlotIndex(event.getSlot()) < 0) {
            return;
        }
        refresh((EntityPlayer) event.getEntityLiving());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        refresh(event.player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.player);
    }

    private ArmorTraitSnapshot getSnapshot(EntityPlayer player) {
        if (player == null) {
            return ArmorTraitSnapshot.EMPTY;
        }
        ArmorTraitSnapshot snapshot = cache.get(player.getUniqueID());
        if (snapshot == null) {
            snapshot = buildSnapshot(player);
            cache.put(player.getUniqueID(), snapshot);
        }
        return snapshot;
    }

    private ArmorTraitSnapshot buildSnapshot(EntityPlayer player) {
        String[][] slotTraits = new String[ARMOR_SLOTS.length][];
        Set<String>[] slotTraitSets = new Set[ARMOR_SLOTS.length];
        Set<String> allTraitSet = new HashSet<>();
        int allCount = 0;

        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            ItemStack stack = player.getItemStackFromSlot(ARMOR_SLOTS[i]);
            String[] traits = TicToolStacks.isTicArmor(stack) ? TicToolTraits.getTraits(stack) : TicToolNbt.EMPTY_STRINGS;
            slotTraits[i] = traits;
            slotTraitSets[i] = toSet(traits);
            allTraitSet.addAll(slotTraitSets[i]);
            allCount += traits.length;
        }

        String[] allTraits = new String[allCount];
        int offset = 0;
        for (String[] traits : slotTraits) {
            System.arraycopy(traits, 0, allTraits, offset, traits.length);
            offset += traits.length;
        }

        return new ArmorTraitSnapshot(slotTraits, allTraits, allTraitSet, slotTraitSets);
    }

    private static Set<String> toSet(String[] traits) {
        if (traits == null || traits.length == 0) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(traits)));
    }

    private static final class ArmorTraitSnapshot {
        static final ArmorTraitSnapshot EMPTY = new ArmorTraitSnapshot(
                new String[][]{
                        TicToolNbt.EMPTY_STRINGS,
                        TicToolNbt.EMPTY_STRINGS,
                        TicToolNbt.EMPTY_STRINGS,
                        TicToolNbt.EMPTY_STRINGS
                },
                TicToolNbt.EMPTY_STRINGS,
                Collections.emptySet(),
                new Set[]{
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet()
                }
        );

        private final String[][] slotTraits;
        private final String[] allTraits;
        private final Set<String> allTraitSet;
        private final Set<String>[] slotTraitSets;

        private ArmorTraitSnapshot(String[][] slotTraits, String[] allTraits, Set<String> allTraitSet, Set<String>[] slotTraitSets) {
            this.slotTraits = slotTraits;
            this.allTraits = allTraits;
            this.allTraitSet = Collections.unmodifiableSet(new HashSet<>(allTraitSet));
            this.slotTraitSets = slotTraitSets;
        }

        private String[] copyAllTraits() {
            return allTraits.length == 0 ? TicToolNbt.EMPTY_STRINGS : Arrays.copyOf(allTraits, allTraits.length);
        }

        private String[] copySlotTraits(int index) {
            String[] traits = slotTraits[index];
            return traits.length == 0 ? TicToolNbt.EMPTY_STRINGS : Arrays.copyOf(traits, traits.length);
        }
    }
}
