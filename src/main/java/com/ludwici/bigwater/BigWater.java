package com.ludwici.bigwater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.Tuple;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Mod(BigWater.MODID)
public class BigWater {
    public static final String MODID = "bigwater";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Map<String, Tuple<Integer, Float>> textureScales = HashMap.newHashMap(8);
    private static List<String> failedLookups = new LinkedList<>();
    public static float defaultScalant = 1.0f;
    public static int defaultTextureScale = 1;
    
    public static Map<String, TextureAtlasSprite> fluidTextures = HashMap.newHashMap(8);

    public BigWater(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::addPackFinders);
        modEventBus.addListener(this::loadingEvent);
        modEventBus.addListener(this::reloadingEvent);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    public void loadingEvent(ModConfigEvent.Loading event) {
        reloadPackSettings();
    }

    public void reloadingEvent(ModConfigEvent.Reloading event) {
        reloadPackSettings();
    }

    public static void reloadPackSettings() {
        defaultTextureScale = Config.DEFAULT_TEXTURE_SCALE.get();
        defaultScalant = 1.0f / defaultTextureScale;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return;
        }

        minecraft.levelRenderer.allChanged();
    }

    public void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }

        internalAddPack(event, "rekindled");
        internalAddPack(event, "stylized");
        internalAddPack(event, "vanilla");
    }

    private void internalAddPack(AddPackFindersEvent event, String name) {
        event.addPackFinders(
                Identifier.fromNamespaceAndPath(MODID, "resourcepacks/" + name),
                PackType.CLIENT_RESOURCES,
                Component.literal(name),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }

    public static TextureAtlasSprite getTexture(String identifier){
        if (fluidTextures.containsKey(identifier)){
            return fluidTextures.get(identifier);
        }
        if (!failedLookups.contains(identifier)){
            failedLookups.add(identifier);
            LOGGER.info("[BigWater] Texture lookup failed for {}, using default", identifier);
        }
        return null;
    }

    public static Tuple<Integer, Float> getTextureScale(String identifier){
        if (Config.OVERRIDE.getAsBoolean()){
            return new Tuple<>(defaultTextureScale, defaultScalant);
        }
        if (textureScales.containsKey(identifier)){
            return textureScales.get(identifier);
        }
        if (!failedLookups.contains(identifier)){
            failedLookups.add(identifier);
            LOGGER.info("[BigWater] Scale lookup failed for {}, using config default", identifier);
        }
        return new Tuple<>(defaultTextureScale, defaultScalant);
    }

    public static int getTexPos(int worldPos, int textureScale, boolean reverseCoords){
        int texPos = worldPos % textureScale;
        if (texPos < 0) texPos = textureScale + texPos;
        if (reverseCoords) texPos = reverseCoord(texPos, textureScale);
        return texPos;
    }

    public static float modCoord(float src, int relativePos, float origin, float sideLength, float scalant){
        return (((src + (sideLength * relativePos) - origin) * scalant)) + origin;
    }

    public static int reverseCoord(int pos, int textureScale){
        return (textureScale - pos) - 1;
    }
}
