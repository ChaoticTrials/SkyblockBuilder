package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "WORLD_PRESET_REGISTRY")
public class SkyblockWorldPresets {

    public static final WorldPreset skyblock = new SkyblockPreset();
}
