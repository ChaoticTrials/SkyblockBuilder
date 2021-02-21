package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.event.RegistryEvent;

import javax.annotation.Nonnull;

public class VoidWorldType extends ForgeWorldType {
    public VoidWorldType() {
        super(VoidWorldType::getChunkGenerator);
    }

    public static void register(RegistryEvent.Register<ForgeWorldType> event) {
        event.getRegistry().register(new VoidWorldType().setRegistryName("custom_skyblock"));
    }

    private static ChunkGenerator getChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        return new VoidChunkGenerator(new SkyblockBiomeProvider(new OverworldBiomeProvider(seed, false, false, biomeRegistry)), seed,
                () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242734_c));
    }

    @Override
    public String getTranslationKey() {
        return SkyblockBuilder.MODID + "generator.custom_skyblock";
    }
}
