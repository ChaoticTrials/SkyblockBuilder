package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.world.presets.BiomeParametersPreset;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

public class SkyblockBiomeParameters extends RegistryProviderBase {

    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> KEY = ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, SkyblockBuilder.getInstance().id("filtered_overworld"));

    public SkyblockBiomeParameters(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
        this.registries.writableRegistry(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).register(
                KEY, new MultiNoiseBiomeSourceParameterList(BiomeParametersPreset.FILTERED_OVERWORLD, this.registries.registry(Registries.BIOME)),
                RegistrationInfo.BUILT_IN
        );
    }

    @Override
    public String getName() {
        return SkyblockBuilder.getInstance().modid + " biome parameters";
    }
}
