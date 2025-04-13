package de.melanx.skyblockbuilder.config.common;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig(value = "client", client = true)
public class ClientConfig {

    @Config("Should the experimental warning pop up on every new world creation? No, I don't think so, but you can re-enable it.")
    public static boolean disableExperimentalWarning = true;

    @Config
    public static boolean allowAprilFools = true;
}
