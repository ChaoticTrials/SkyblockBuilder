package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.util.ListHandler;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.util.ResourceLocation;
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
import org.apache.logging.log4j.Logger;

@Mod("skyblockbuilder")
public class SkyblockBuilder extends ModXRegistration {

    private static SkyblockBuilder instance;
    public static final Gson PRETTY_GSON = Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setLenient();
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public SkyblockBuilder() {
        super("skyblockbuilder", null);
        instance = this;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onConfigChange);

        ConfigHandler.createDirectories();
        // TODO 1.17 switch to LibX config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG, "skyblockbuilder/config.toml");

        MinecraftForge.EVENT_BUS.register(new EventListener());

        ConfigManager.registerConfig(new ResourceLocation("skyblockbuilder", "test"), LibXConfigHandler.class, false);
    }

    private void onConfigChange(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getModId().equals(this.modid)) {
            ListHandler.initLists();
        }
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.register();
        }

        Registration.registerCodecs();
        ConfigHandler.generateDefaultFiles();
        ListHandler.initLists();
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        // not now
    }

    public static SkyblockBuilder getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return instance.logger;
    }
}
