package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.Registration;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.levelgen.*;
import net.minecraftforge.common.world.ForgeWorldPreset;

public class SkylandsType extends ForgeWorldPreset {

    public static final NoiseSettings NOISE_SETTINGS = NoiseSettings.create(
            -64,
            192,
            new NoiseSamplingSettings(3.5, 1.1, 400, 120),
            new NoiseSlider(-0.154, 28, 2),
            new NoiseSlider(-0.375, 32, 1),
            2,
            1,
            new TerrainShaper(CubicSpline.constant(0.15f), CubicSpline.constant(0.035f), CubicSpline.constant(0.075f))
    );
    public static final NoiseRouterWithOnlyNoises NOISE_ROUTER = new NoiseRouterWithOnlyNoises(
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_BARRIER), 1, 0.5),
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 1, 0.67),
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 1, (double) 5 / 7),
            DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_LAVA)),
            DensityFunctions.shiftedNoise2d(
                    NoiseRouterData.getFunction(NoiseRouterData.SHIFT_X),
                    NoiseRouterData.getFunction(NoiseRouterData.SHIFT_Z),
                    0.25,
                    NoiseRouterData.getNoise(Noises.TEMPERATURE)),
            DensityFunctions.shiftedNoise2d(
                    NoiseRouterData.getFunction(NoiseRouterData.SHIFT_X),
                    NoiseRouterData.getFunction(NoiseRouterData.SHIFT_Z),
                    0.25,
                    NoiseRouterData.getNoise(Noises.VEGETATION)),
            NoiseRouterData.getFunction(NoiseRouterData.CONTINENTS),
            NoiseRouterData.getFunction(NoiseRouterData.EROSION),
            NoiseRouterData.getFunction(NoiseRouterData.DEPTH),
            NoiseRouterData.getFunction(NoiseRouterData.RIDGES),
            DensityFunctions.mul(
                    DensityFunctions.constant(4),
                    DensityFunctions.Mapped.create(
                            DensityFunctions.Mapped.Type.QUARTER_NEGATIVE,
                            DensityFunctions.mul(
                                    NoiseRouterData.getFunction(NoiseRouterData.DEPTH),
                                    DensityFunctions.cache2d(NoiseRouterData.getFunction(NoiseRouterData.FACTOR))
                            )
                    )
            ),
            DensityFunctions.min(DensityFunctions.Mapped.create(
                            DensityFunctions.Mapped.Type.SQUEEZE,
                            DensityFunctions.mul(
                                    DensityFunctions.constant(0.64),
                                    DensityFunctions.interpolated(
                                            DensityFunctions.blendDensity(
                                                    DensityFunctions.slide(
                                                            null,
                                                            DensityFunctions.rangeChoice(
                                                                    NoiseRouterData.getFunction(NoiseRouterData.SLOPED_CHEESE),
                                                                    -1000000,
                                                                    (double) 25 / 16,
                                                                    DensityFunctions.min(
                                                                            NoiseRouterData.getFunction(NoiseRouterData.SLOPED_CHEESE),
                                                                            DensityFunctions.mul(
                                                                                    DensityFunctions.constant(10),
                                                                                    NoiseRouterData.getFunction(NoiseRouterData.ENTRANCES)
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
                                                                                                                                    NoiseRouterData.getFunction(NoiseRouterData.SLOPED_CHEESE)
                                                                                                                            )
                                                                                                                    ), 0, 0.5
                                                                                                            )
                                                                                                    )
                                                                                            ),
                                                                                            NoiseRouterData.getFunction(NoiseRouterData.ENTRANCES)
                                                                                    ),
                                                                                    DensityFunctions.add(
                                                                                            NoiseRouterData.getFunction(NoiseRouterData.SPAGHETTI_2D),
                                                                                            NoiseRouterData.getFunction(NoiseRouterData.SPAGHETTI_ROUGHNESS_FUNCTION)
                                                                                    )
                                                                            ),
                                                                            DensityFunctions.rangeChoice(
                                                                                    NoiseRouterData.getFunction(NoiseRouterData.PILLARS),
                                                                                    -100000,
                                                                                    0.03,
                                                                                    DensityFunctions.constant(-1000000),
                                                                                    NoiseRouterData.getFunction(NoiseRouterData.PILLARS)
                                                                            )
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    ),
                    NoiseRouterData.getFunction(NoiseRouterData.NOODLE)),
            DensityFunctions.interpolated(
                    DensityFunctions.rangeChoice(
                            NoiseRouterData.getFunction(NoiseRouterData.Y),
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
                                                    NoiseRouterData.getFunction(NoiseRouterData.Y),
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
                                                    NoiseRouterData.getFunction(NoiseRouterData.Y),
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

    public SkylandsType() {
        super((reg, seed) -> WorldGenSettings.makeOverworld(reg, seed, Registration.SKYLANDS_SETTINGS));
    }
}
