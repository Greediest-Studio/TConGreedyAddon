package com.smd.tcongreedyaddon.traits.something;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.plugin.something.something;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TraitCleverTranslation extends AbstractTrait {

    private static final String NBT_X = "CleverTranslationX";
    private static final String NBT_LAST_TICK = "CleverTranslationLastTick";
    private static final String NBT_LAST_SOURCE = "CleverTranslationSource";
    private static final String NBT_LAST_TARGET = "CleverTranslationTarget";

    private static final int TRANSLATION_INTERVAL_TICKS = 20 * 60;
    private static final float MAX_X = 15.0F;

    private static final ExecutorService TRANSLATION_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger index = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "tcongreedyaddon-translation-" + index.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    public TraitCleverTranslation() {
        super("clever_translation", TextFormatting.AQUA);
    }

    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote) {
            return;
        }
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase living = (EntityLivingBase) entity;
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
        if (currentTick - lastTick < TRANSLATION_INTERVAL_TICKS) {
            return;
        }

        String source = tool.getDisplayName();
        if (source == null || source.trim().isEmpty()) {
            return;
        }

        String appId = something.baiduAppId;
        String appKey = something.baiduAppKey;
        if (appId == null || appId.isEmpty() || appKey == null || appKey.isEmpty()) {
            return;
        }

        tag.setLong(NBT_LAST_TICK, currentTick);

        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return;
        }

        UUID playerId = living instanceof EntityPlayer ? living.getUniqueID() : null;
        if (playerId != null) {
            server.addScheduledTask(() -> sendMessageToPlayer(server, playerId, "[机翻有理] 开始翻译：" + source));
        }

        scheduleTranslation(tool, server, source, appId, appKey, playerId);
    }

    @Override
    public float damage(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, float newDamage, boolean isCritical) {
        NBTTagCompound tag = tool.getTagCompound();
        if (tag == null) {
            return super.damage(tool, player, target, damage, newDamage, isCritical);
        }

        float xValue = tag.getFloat(NBT_X);
        if (xValue <= 0.0F) {
            return super.damage(tool, player, target, damage, newDamage, isCritical);
        }

        float capped = Math.min(MAX_X, xValue);
        float multiplier = 1.0F + (0.2F * capped);
        return super.damage(tool, player, target, damage, newDamage * multiplier, isCritical);
    }

    private void scheduleTranslation(ItemStack tool, MinecraftServer server, String source, String appId, String appKey, UUID playerId) {
        TRANSLATION_EXECUTOR.submit(() -> {
            try {
                TranslationResult result = translateWithBaidu(source, appId, appKey);
                if (!result.success || result.translated == null || result.translated.trim().isEmpty()) {
                    String reason = result.errorMessage == null ? "未知错误" : result.errorMessage;
                    if (playerId != null) {
                        server.addScheduledTask(() -> sendMessageToPlayer(server, playerId, "[机翻有理] 翻译失败：" + reason));
                    }
                    return;
                }

                int sourceLength = source.length();
                if (sourceLength <= 0) {
                    return;
                }

                String translated = result.translated;
                int translatedLength = translated.length();
                float ratio = (float) translatedLength / (float) sourceLength;
                float xValue = Math.min(MAX_X, ratio);

                server.addScheduledTask(() -> {
                    if (tool.isEmpty()) {
                        return;
                    }

                    NBTTagCompound tag = tool.getTagCompound();
                    if (tag == null) {
                        tag = new NBTTagCompound();
                        tool.setTagCompound(tag);
                    }

                    tag.setFloat(NBT_X, xValue);
                    tag.setString(NBT_LAST_SOURCE, source);
                    tag.setString(NBT_LAST_TARGET, translated);

                    if (playerId != null) {
                        sendMessageToPlayer(server, playerId, "[机翻有理] 翻译完成：" + translated + " (X=" + String.format(Locale.ROOT, "%.2f", xValue) + ")");
                    }
                });
            } catch (Exception e) {
                TConGreedyAddon.LOGGER.warn("CleverTranslation: translate failed", e);
                if (playerId != null) {
                    server.addScheduledTask(() -> sendMessageToPlayer(server, playerId, "[机翻有理] 翻译失败：" + e.getMessage()));
                }
            }
        });
    }

    private TranslationResult translateWithBaidu(String source, String appId, String appKey) throws Exception {
        String salt = String.valueOf(System.currentTimeMillis());
        String sign = md5(appId + source + salt + appKey);
        String encodedQuery = URLEncoder.encode(source, StandardCharsets.UTF_8.name());

        String url = String.format(Locale.ROOT,
                "https://fanyi-api.baidu.com/api/trans/vip/translate?q=%s&from=zh&to=en&appid=%s&salt=%s&sign=%s",
                encodedQuery, appId, salt, sign);

        HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int status = connection.getResponseCode();
        if (status != 200) {
            return TranslationResult.fail("HTTP " + status);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
        if (json.has("error_code")) {
            String code = json.get("error_code").getAsString();
            String msg = json.has("error_msg") ? json.get("error_msg").getAsString() : "unknown";
            return TranslationResult.fail(code + ":" + msg);
        }
        if (!json.has("trans_result")) {
            return TranslationResult.fail("no trans_result");
        }
        JsonArray results = json.getAsJsonArray("trans_result");
        if (results.size() == 0) {
            return TranslationResult.fail("empty trans_result");
        }
        JsonObject first = results.get(0).getAsJsonObject();
        if (!first.has("dst")) {
            return TranslationResult.fail("missing dst");
        }
        return TranslationResult.ok(first.get("dst").getAsString());
    }

    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte b : digest) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private void sendMessageToPlayer(MinecraftServer server, UUID playerId, String message) {
        if (server == null || playerId == null) {
            return;
        }
        EntityPlayer player = server.getPlayerList().getPlayerByUUID(playerId);
        if (player != null) {
            player.sendMessage(new TextComponentString(message));
        }
    }

    private static class TranslationResult {
        private final boolean success;
        private final String translated;
        private final String errorMessage;

        private TranslationResult(boolean success, String translated, String errorMessage) {
            this.success = success;
            this.translated = translated;
            this.errorMessage = errorMessage;
        }

        private static TranslationResult ok(String translated) {
            return new TranslationResult(true, translated, null);
        }

        private static TranslationResult fail(String message) {
            return new TranslationResult(false, null, message);
        }
    }
}
