package de.melanx.skyblockbuilder.config.common;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig(requiresMod = "cadmus")
public class CadmusConfig {

    @Config({"The chunks of the spawn will be claimed automatically as admin claims.",
            "The radius is based on the spawnProtectionRadius."})
    public static boolean protectSpawnChunks = true;
}
