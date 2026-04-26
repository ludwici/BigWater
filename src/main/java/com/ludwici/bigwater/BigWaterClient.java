package com.ludwici.bigwater;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.ludwici.bigwater.BigWater.*;

@Mod(value = MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BigWaterClient {
    public BigWaterClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void addReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MODID, "config"), (ResourceManagerReloadListener) manager -> {
            textureScales.clear();
            Map<Identifier, Resource> resourceMap = manager.listResources("config", path -> path.toString().endsWith("bigwater.json"));
            for(Map.Entry<Identifier, Resource> entry : resourceMap.entrySet()){
                try(InputStream stream = manager.getResource(entry.getKey()).get().open()) {
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    JsonObject json = GsonHelper.parse(streamReader);

                    JsonObject settings = json.get("textureScale").getAsJsonObject();
                    for (String key : settings.keySet()){
                        int value = settings.get(key).getAsInt();
                        textureScales.put(key, new Tuple<>(value, 1.0f/value));
                        String[] split = key.split(":");
                        if (split.length > 1) {
                            textureScales.put(split[0] + ":flowing_" + split[1], new Tuple<>(value, 1.0f / value));
                        }
                    }
                    LOGGER.info("[BigWaterFork] Read resource pack provided settings");

                } catch(Exception e) {
                    LOGGER.error("[BigWaterFork] Failed to read resource pack settings");
                    LOGGER.error(String.valueOf(e));
                }
            }
            fluidTextures.clear();
            checkCustomTextures("water"); // TODO: make this run for any registered fluids
            checkCustomTextures("lava");
        });
    }

    private static void checkCustomTextures(String blockID){
        TextureAtlasSprite stillSprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(Identifier.fromNamespaceAndPath(MODID,"block/" + blockID + "_still"));
        if (stillSprite.contents().name().toString().equals("minecraft:missingno")) stillSprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(Identifier.fromNamespaceAndPath("minecraft","block/" + blockID + "_still"));
        fluidTextures.put("minecraft:block/"+blockID+"_still", stillSprite);
        TextureAtlasSprite flowSprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(Identifier.fromNamespaceAndPath(MODID,"block/" + blockID + "_flow"));
        if (flowSprite.contents().name().toString().equals("minecraft:missingno")) flowSprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(Identifier.fromNamespaceAndPath("minecraft","block/" + blockID + "_flow"));
        fluidTextures.put("minecraft:block/"+blockID+"_flow", flowSprite);
    }

}
