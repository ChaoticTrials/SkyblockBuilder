package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

public class WorldPresetProvider extends RegistryProviderBase {

    public WorldPresetProvider(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
        this.registries.writableRegistry(Registries.WORLD_PRESET).register(SkyblockPreset.KEY, new SkyblockPreset(
                this.registries.registry(Registries.DIMENSION_TYPE).asLookup(),
                this.registries.registry(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).asLookup(),
                this.registries.registry(Registries.NOISE_SETTINGS).asLookup(),
                this.registries.registry(Registries.BIOME).asLookup()
        ), RegistrationInfo.BUILT_IN);
    }

    @Override
    public String getName() {
        return this.mod.modid + " world presets";
    }
}
