package com.smd.tcongreedyaddon.traits.something;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TraitConnection404 extends AbstractTrait {

    private static final String NBT_LOSS_RATE = "Connection404LossRate";
    private static final String NBT_LAST_TICK = "Connection404LastTick";
    private static final String NBT_TOTAL = "Connection404Samples";
    private static final String NBT_LOSSES = "Connection404Losses";

    private static final int PING_INTERVAL_TICKS = 20 * 120;
    private static final int MAX_SAMPLES = 20;

    private static final ExecutorService PING_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger index = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "tcongreedyaddon-ping-" + index.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    public TraitConnection404() {
        super("connection_404", TextFormatting.DARK_GRAY);
    }

    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote) {
            return;
        }
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        if (tool.isEmpty()) {
            return;
        }

        NBTTagCompound tag = tool.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            tool.setTagCompound(tag);
        }

        long currentTick = world.getTotalWorldTime();
        long lastTick = tag.getLong(NBT_LAST_TICK);
        if (currentTick - lastTick < PING_INTERVAL_TICKS) {
            return;
        }

        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return;
        }

        tag.setLong(NBT_LAST_TICK, currentTick);
        schedulePing(tool, server);
    }

    @Override
    public float damage(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, float newDamage, boolean isCritical) {
        NBTTagCompound tag = tool.getTagCompound();
        if (tag == null) {
            return super.damage(tool, player, target, damage, newDamage, isCritical);
        }

        float lossRate = tag.getFloat(NBT_LOSS_RATE);
        if (lossRate < 0.0F) {
            lossRate = 0.0F;
        } else if (lossRate > 1.0F) {
            lossRate = 1.0F;
        }

        float multiplier = 1.75F - lossRate;
        if (multiplier < 0.0F) {
            multiplier = 0.0F;
        }

        return super.damage(tool, player, target, damage, newDamage * multiplier, isCritical);
    }

    private void schedulePing(ItemStack tool, MinecraftServer server) {
        PING_EXECUTOR.submit(() -> {
            boolean success;
            try {
                success = pingGithub();
            } catch (Exception e) {
                TConGreedyAddon.LOGGER.warn("Connection404: ping failed", e);
                success = false;
            }

            boolean finalSuccess = success;
            server.addScheduledTask(() -> {
                if (tool.isEmpty()) {
                    return;
                }

                NBTTagCompound tag = tool.getTagCompound();
                if (tag == null) {
                    tag = new NBTTagCompound();
                    tool.setTagCompound(tag);
                }

                int total = tag.getInteger(NBT_TOTAL);
                int losses = tag.getInteger(NBT_LOSSES);

                total += 1;
                if (!finalSuccess) {
                    losses += 1;
                }

                if (total > MAX_SAMPLES) {
                    total = Math.max(1, total / 2);
                    losses = Math.max(0, losses / 2);
                }

                float lossRate = total > 0 ? (float) losses / (float) total : 0.0F;

                tag.setInteger(NBT_TOTAL, total);
                tag.setInteger(NBT_LOSSES, losses);
                tag.setFloat(NBT_LOSS_RATE, lossRate);
            });
        });
    }

    private boolean pingGithub() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new java.net.URL("https://github.com").openConnection();
        connection.setRequestMethod("HEAD");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        int status = connection.getResponseCode();
        return status >= 200 && status < 400;
    }
}
