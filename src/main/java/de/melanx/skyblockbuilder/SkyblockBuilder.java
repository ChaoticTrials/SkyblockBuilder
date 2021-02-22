package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import de.melanx.skyblockbuilder.world.nether.SkyblockNetherBiomeProvider;
import de.melanx.skyblockbuilder.world.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.overworld.SkyblockBiomeProvider;
import de.melanx.skyblockbuilder.world.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldType;
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

    public static final Gson PRETTY_GSON = Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setLenient();
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public SkyblockBuilder() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        bus.addGenericListener(ForgeWorldType.class, VoidWorldType::register);

        ConfigHandler.generateDefaultFiles();

        MinecraftForge.EVENT_BUS.register(new EventListener());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SkyblockBiomeProvider.init();
            SkyblockNetherBiomeProvider.init();
            SkyblockOverworldChunkGenerator.init();
            SkyblockNetherChunkGenerator.init();
        });
    }
}
