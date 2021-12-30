package de.melanx.skyblockbuilder.config;

public class SpawnSettings {

    public record Range(int bottom, int top) {
    }

    public enum Type {
        SET,
        RANGE_BOTTOM,
        RANGE_TOP
    }
}
