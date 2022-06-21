package de.melanx.skyblockbuilder.world;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;

public class SkylandsType extends WorldPreset {

    public static final NoiseSettings NOISE_SETTINGS = NoiseSettings.create(
            -64,
            192,
//            new NoiseSamplingSettings(3.5, 1.1, 400, 120),
//            new NoiseSlider(-0.154, 28, 2),
//            new NoiseSlider(-0.375, 32, 1),
            2,
            1
//            new TerrainShaper(CubicSpline.constant(0.15f), CubicSpline.constant(0.035f), CubicSpline.constant(0.075f))
    );
    public static final NoiseRouter NOISE_ROUTER = new NoiseRouter(
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_BARRIER), 1, 0.5),
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 1, 0.67),
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 1, (double) 5 / 7),
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_LAVA)),
            DensityFunctions.shiftedNoise2d(
                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SHIFT_X),
                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SHIFT_Z),
                    0.25,
                    NoiseRouterData.getNoise(Noises.TEMPERATURE)),
            DensityFunctions.shiftedNoise2d(
                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SHIFT_X),
                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SHIFT_Z),
                    0.25,
                    NoiseRouterData.getNoise(Noises.VEGETATION)),
            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.CONTINENTS),
            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.EROSION),
            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.DEPTH),
            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.RIDGES),
            DensityFunctions.mul(
                    DensityFunctions.constant(4),
                    DensityFunctions.Mapped.create(
                            DensityFunctions.Mapped.Type.QUARTER_NEGATIVE,
                            DensityFunctions.mul(
                                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.DEPTH),
                                    DensityFunctions.cache2d(NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.FACTOR))
                            )
                    )
            ),
            DensityFunctions.min(DensityFunctions.Mapped.create(
                            DensityFunctions.Mapped.Type.SQUEEZE,
                            DensityFunctions.mul(
                                    DensityFunctions.constant(0.64),
                                    DensityFunctions.interpolated(
                                            DensityFunctions.blendDensity(
                                                    DensityFunctions.rangeChoice(
                                                            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SLOPED_CHEESE),
                                                            -1000000,
                                                            (double) 25 / 16,
                                                            DensityFunctions.min(
                                                                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SLOPED_CHEESE),
                                                                    DensityFunctions.mul(
                                                                            DensityFunctions.constant(10),
                                                                            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.ENTRANCES)
                                                                    )),
                                                            DensityFunctions.max(
                                                                    DensityFunctions.min(
                                                                            DensityFunctions.min(
                                                                                    DensityFunctions.add(
                                                                                            DensityFunctions.mul(
                                                                                                    DensityFunctions.constant(4),
                                                                                                    DensityFunctions.Mapped.create(
                                                                                                            DensityFunctions.Mapped.Type.SQUARE,
                                                                                                            DensityFunctions.noise(
                                                                                                                    NoiseRouterData.getNoise(Noises.CAVE_LAYER),
                                                                                                                    1,
                                                                                                                    8
                                                                                                            )
                                                                                                    )
                                                                                            ),
                                                                                            DensityFunctions.add(
                                                                                                    new DensityFunctions.Clamp(
                                                                                                            DensityFunctions.add(
                                                                                                                    DensityFunctions.constant(0.27),
                                                                                                                    DensityFunctions.noise(
                                                                                                                            NoiseRouterData.getNoise(Noises.CAVE_CHEESE),
                                                                                                                            1,
                                                                                                                            (double) 2 / 3
                                                                                                                    )
                                                                                                            ), -1, 1
                                                                                                    ),
                                                                                                    new DensityFunctions.Clamp(
                                                                                                            DensityFunctions.add(
                                                                                                                    DensityFunctions.constant(1.5),
                                                                                                                    DensityFunctions.mul(
                                                                                                                            DensityFunctions.constant(-0.64),
                                                                                                                            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SLOPED_CHEESE)
                                                                                                                    )
                                                                                                            ), 0, 0.5
                                                                                                    )
                                                                                            )
                                                                                    ),
                                                                                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.ENTRANCES)
                                                                            ),
                                                                            DensityFunctions.add(
                                                                                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SPAGHETTI_2D),
                                                                                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.SPAGHETTI_ROUGHNESS_FUNCTION)
                                                                            )
                                                                    ),
                                                                    DensityFunctions.rangeChoice(
                                                                            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.PILLARS),
                                                                            -100000,
                                                                            0.03,
                                                                            DensityFunctions.constant(-1000000),
                                                                            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.PILLARS)
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    ),
                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.NOODLE)),
            DensityFunctions.interpolated(
                    DensityFunctions.rangeChoice(
                            NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.Y),
                            -60,
                            51,
                            DensityFunctions.noise(
                                    NoiseRouterData.getNoise(Noises.ORE_VEININESS),
                                    1.5,
                                    1.5
                            ),
                            DensityFunctions.constant(0)
                    )
            ),
            DensityFunctions.add(
                    DensityFunctions.constant(-0.08),
                    DensityFunctions.max(
                            DensityFunctions.Mapped.create(
                                    DensityFunctions.Mapped.Type.ABS,
                                    DensityFunctions.interpolated(
                                            DensityFunctions.rangeChoice(
                                                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.Y),
                                                    -60,
                                                    51,
                                                    DensityFunctions.noise(
                                                            NoiseRouterData.getNoise(Noises.ORE_VEIN_A),
                                                            4,
                                                            4
                                                    ),
                                                    DensityFunctions.constant(0)
                                            )
                                    )
                            ),
                            DensityFunctions.Mapped.create(
                                    DensityFunctions.Mapped.Type.ABS,
                                    DensityFunctions.interpolated(
                                            DensityFunctions.rangeChoice(
                                                    NoiseRouterData.getFunction(BuiltinRegistries.DENSITY_FUNCTION, NoiseRouterData.Y),
                                                    -60,
                                                    51,
                                                    DensityFunctions.noise(
                                                            NoiseRouterData.getNoise(Noises.ORE_VEIN_B),
                                                            4,
                                                            4
                                                    ),
                                                    DensityFunctions.constant(0)
                                            )
                                    )
                            )
                    )
            ),
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.ORE_GAP))
    );

    // TODO check
    public SkylandsType() {
        super(Map.of());
//        super((reg, seed) -> {
//            Registry<Biome> biomes = reg.registryOrThrow(Registry.BIOME_REGISTRY);
//            Registry<StructureSet> structureSets = reg.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
//            Registry<NoiseGeneratorSettings> noiseGeneratorSettings = reg.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
//            Registry<NormalNoise.NoiseParameters> noiseParameters = reg.registryOrThrow(Registry.NOISE_REGISTRY);
//            return new NoiseBasedChunkGenerator(structureSets, noiseParameters, MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomes, true), noiseGeneratorSettings.getOrCreateHolder(Registration.SKYLANDS_SETTINGS).getOrThrow(false, System.out::println));
//        });
    }
}
