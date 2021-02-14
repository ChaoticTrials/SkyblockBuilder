package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.world.VoidChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SkyblockBuilder.MODID)
public class SkyblockBuilder {

    public static final String MODID = "skyblockbuilder";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public SkyblockBuilder() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new EventListener());

        Registration.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(VoidChunkGenerator::init);
    }
}
