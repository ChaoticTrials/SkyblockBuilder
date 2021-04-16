package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListHandler {

    public static List<ResourceLocation> WHITELIST_STRUCTURES;
    public static List<ResourceLocation> WHITELIST_FEATURES;

    public static void initLists() {
        String[] whitelistStructures = ConfigHandler.whitelistStructures.get().toArray(new String[0]);
        WHITELIST_STRUCTURES = new ArrayList<>();

        for (String s : whitelistStructures) {
            if (s.contains(":")) {
                WHITELIST_STRUCTURES.add(new ResourceLocation(s));
            } else {
                WHITELIST_STRUCTURES.addAll(ForgeRegistries.STRUCTURE_FEATURES.getKeys().stream()
                        .filter(location -> location.getNamespace().equals(s))
                        .collect(Collectors.toList())
                );
            }
        }
        SkyblockBuilder.getLogger().info("Whitelisted structures: " + Arrays.toString(WHITELIST_STRUCTURES.toArray()));

        String[] whitelistFeatures = ConfigHandler.whitelistFeatures.get().toArray(new String[0]);
        WHITELIST_FEATURES = new ArrayList<>();

        for (String s : whitelistFeatures) {
            if (s.contains(":")) {
                WHITELIST_FEATURES.add(new ResourceLocation(s));
            } else {
                WHITELIST_FEATURES.addAll(ForgeRegistries.FEATURES.getKeys().stream()
                        .filter(location -> location.getNamespace().equals(s))
                        .collect(Collectors.toList())
                );
            }
        }
        SkyblockBuilder.getLogger().info("Whitelisted features: " + Arrays.toString(WHITELIST_FEATURES.toArray()));
    }
}
