package de.melanx.skyblockbuilder.config.common;

import de.melanx.skyblockbuilder.compat.CadmusCompat;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig(value = CadmusCompat.MODID, requiresMod = CadmusCompat.MODID)
public class CadmusConfig {

    @Config({"The chunks of the spawn will be claimed automatically as admin claims.",
            "The radius is based on the spawnProtectionRadius."})
    public static boolean protectSpawnChunks = true;
}
