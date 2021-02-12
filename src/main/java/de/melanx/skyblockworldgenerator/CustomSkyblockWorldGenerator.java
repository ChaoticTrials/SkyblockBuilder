package de.melanx.skyblockworldgenerator;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomSkyblockWorldGenerator.MODID)
public class CustomSkyblockWorldGenerator {

    public static final String MODID = "skyblockworldgenerator";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    public CustomSkyblockWorldGenerator instance;

    public CustomSkyblockWorldGenerator() {
        instance = this;

        MinecraftForge.EVENT_BUS.register(this);
    }
}
