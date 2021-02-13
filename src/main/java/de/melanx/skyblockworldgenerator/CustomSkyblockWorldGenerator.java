package de.melanx.skyblockworldgenerator;

import de.melanx.skyblockworldgenerator.world.VoidChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomSkyblockWorldGenerator.MODID)
public class CustomSkyblockWorldGenerator {

    public static final String MODID = "skyblockworldgenerator";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CustomSkyblockWorldGenerator() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new EventListener());

        Registration.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(VoidChunkGenerator::init);
    }
}
