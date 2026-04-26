package com.ludwici.bigwater;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue OVERRIDE = BUILDER.define("override", false);
    public static final ModConfigSpec.ConfigValue<Integer> DEFAULT_TEXTURE_SCALE = BUILDER.define("defaultTextureScale", 1);

    static final ModConfigSpec SPEC = BUILDER.build();
}
