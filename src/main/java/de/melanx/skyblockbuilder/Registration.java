package de.melanx.skyblockbuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class Registration {

    public static final ResourceKey<WorldPreset> skyblockKey = ResourceKey.create(Registries.WORLD_PRESET, SkyblockBuilder.getInstance().resource("skyblock"));
}
