package de.melanx.skyblockbuilder.util;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class CoreUtil {

    public static void afterWorldGenLayerLoad(LayeredRegistryAccess<RegistryLayer> access) {
        MappedRegistry<WorldPreset> worldPresetRegistry = (MappedRegistry<WorldPreset>) access.getAccessForLoading(RegistryLayer.DIMENSIONS).registry(Registries.WORLD_PRESET).orElse(null);
        if (worldPresetRegistry != null) {
            //noinspection deprecation
            worldPresetRegistry.unfreeze();
            worldPresetRegistry.register(Registration.skyblockKey, new SkyblockPreset(access.compositeAccess()), Lifecycle.experimental());
            worldPresetRegistry.freeze();
        }
    }
}
