package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.registration.Registration;
import de.melanx.skyblockbuilder.registration.ScreenStructureSaver;
import de.melanx.skyblockbuilder.util.ListHandler;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
        bus.addListener(this::clientSetup);
        bus.addListener(this::commonSetup);
        bus.addListener(this::onConfigChange);
        Registration.init();

        ConfigHandler.createDirectories();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG, SkyblockBuilder.MODID + "/config.toml");

        MinecraftForge.EVENT_BUS.register(new EventListener());
    }

    private void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registration.CONTAINER_STRUCTURE_SAVER.get(), ScreenStructureSaver::new);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SkyblockBiomeProvider.init();
            SkyblockNetherBiomeProvider.init();
            SkyblockEndBiomeProvider.init();

            SkyblockOverworldChunkGenerator.init();
            SkyblockNetherChunkGenerator.init();
            SkyblockEndChunkGenerator.init();

            if (ModList.get().isLoaded("minemention")) {
                MineMentionCompat.register();
            }
        });

        ConfigHandler.generateDefaultFiles();
        ListHandler.initLists();
    }

    private void onConfigChange(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MODID)) {
            ListHandler.initLists();
        }
    }
}
