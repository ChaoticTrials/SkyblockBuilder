package de.melanx.skyblockbuilder.util;

import com.google.common.collect.ImmutableList;
import de.melanx.skyblockbuilder.ConfigHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class RandomUtility {
    public static final ITextComponent UNKNOWN_PLAYER = new TranslationTextComponent("skyblockbuilder.unknown_player");

    public static ITextComponent getDisplayNameByUuid(World world, UUID id) {
        PlayerEntity player = world.getPlayerByUuid(id);
        return player != null ? player.getDisplayName() : UNKNOWN_PLAYER;
    }

    public static Registry<Biome> modifyLookupRegistry(Registry<Biome> registry) {
        registry.getEntries().forEach(biomeEntry -> {
            // Remove non-whitelisted structures
            List<Supplier<StructureFeature<?, ?>>> structures = new ArrayList<>();

            for (Supplier<StructureFeature<?, ?>> structure : biomeEntry.getValue().getGenerationSettings().structures) {
                ResourceLocation location = structure.get().field_236268_b_.getRegistryName();
                if (location != null) {
                    if (ConfigHandler.toggleWhitelist.get()) {
                        if (!ConfigHandler.whitelistStructures.get().contains(location.toString())) {
                            structures.add(structure);
                        }
                    } else {
                        if (ConfigHandler.whitelistStructures.get().contains(location.toString())) {
                            structures.add(structure);
                        }
                    }
                }
            }

            biomeEntry.getValue().getGenerationSettings().structures = ImmutableList.copyOf(structures);

            // Remove non-whitelisted features
            List<Supplier<ConfiguredFeature<?, ?>>> features = new ArrayList<>();

            biomeEntry.getValue().getGenerationSettings().features.forEach(list -> {
                for (Supplier<ConfiguredFeature<?, ?>> feature : list) {
                    ResourceLocation location = feature.get().feature.getRegistryName();
                    if (location != null) {
                        if (ConfigHandler.toggleWhitelist.get()) {
                            if (!ConfigHandler.whitelistFeatures.get().contains(location.toString())) {
                                features.add(feature);
                            }
                        } else {
                            if (ConfigHandler.whitelistFeatures.get().contains(location.toString())) {
                                features.add(feature);
                            }
                        }
                    }
                }
            });

            biomeEntry.getValue().getGenerationSettings().features = ImmutableList.of(features);
        });

        return registry;
    }
}
