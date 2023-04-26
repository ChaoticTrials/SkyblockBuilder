package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.moddingx.libx.annotation.registration.Reg;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "WORLD_PRESET_REGISTRY")
public class SkyblockWorldPresets {

    public static final WorldPreset skyblock = new SkyblockPreset();
    @Reg.Exclude
    public static final ResourceKey<WorldPreset> skyblockKey = ResourceKey.create(Registries.WORLD_PRESET, SkyblockBuilder.getInstance().resource("skyblock"));
}
