package de.melanx.skyblockworldgenerator;

import de.melanx.skyblockworldgenerator.world.VoidChunkGenerator;
import de.melanx.skyblockworldgenerator.world.VoidWorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomSkyblockWorldGenerator.MODID)
public class CustomSkyblockWorldGenerator {

    public static final String MODID = "skyblockworldgenerator";
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public CustomSkyblockWorldGenerator() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        bus.addGenericListener(ForgeWorldType.class, this::registerWorldType);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(VoidChunkGenerator::init);
    }

    public void registerWorldType(RegistryEvent.Register<ForgeWorldType> event) {
        event.getRegistry().register(VoidWorldType.INSTANCE.setRegistryName(MODID, "custom_skyblock"));
    }
}
