package de.melanx.skyblockbuilder.world.flat;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import java.util.Collections;
import java.util.List;

public record FlatLayers(List<FlatLayerConfig> layers) {

    public static final Codec<FlatLayers> CODEC = FlatLayerConfig.CODEC.listOf().xmap(FlatLayers::new, FlatLayers::layers);
    public static final FlatLayers EMPTY = new FlatLayers(List.of());

    public boolean isEmpty() {
        return this == EMPTY || this.layers.isEmpty();
    }

    public int totalHeight() {
        return this.layers.stream().mapToInt(FlatLayerConfig::getHeight).sum();
    }

    public static FlatLayers of(List<FlatLayerConfig> layers) {
        return new FlatLayers(layers);
    }

    public static FlatLayers of(FlatLayerConfig... layers) {
        return new FlatLayers(List.of(layers));
    }

    public static FlatLayers of(String layers) {
        if (layers == null || layers.isBlank()) {
            return EMPTY;
        }

        List<FlatLayerConfig> list = Lists.newArrayList();
        String[] astring = layers.split(",");
        int i = 0;

        for (String s : astring) {
            FlatLayerConfig layerConfig = FlatLayerConfig.getLayerConfig(s, i);
            if (layerConfig == null) {
                return EMPTY;
            }

            list.add(layerConfig);
            i += layerConfig.getHeight();
        }

        return new FlatLayers(Collections.unmodifiableList(list));
    }

    @Override
    public String toString() {
        return this.layers.stream().map(FlatLayerConfig::toString).reduce((a, b) -> a + "," + b).orElse("");
    }
}
