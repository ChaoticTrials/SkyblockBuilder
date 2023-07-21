package de.melanx.skyblockbuilder.datagen;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.core.registries.Registries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

public class WorldPresetProvider extends RegistryProviderBase {
    
    public WorldPresetProvider(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
        this.registries.writableRegistry(Registries.WORLD_PRESET).register(Registration.skyblockKey, new SkyblockPreset(
                this.registries.registry(Registries.DIMENSION_TYPE).asLookup(),
                this.registries.registry(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).asLookup(),
                this.registries.registry(Registries.NOISE_SETTINGS).asLookup(),
                this.registries.registry(Registries.BIOME).asLookup()
        ), Lifecycle.stable());
    }

    @Override
    public String getName() {
        return this.mod.modid + " world presets";
    }
}
