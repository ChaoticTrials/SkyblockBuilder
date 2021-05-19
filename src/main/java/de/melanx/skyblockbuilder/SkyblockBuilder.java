package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.melanx.skyblockbuilder.client.ClientSetup;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.config.ConfigParser;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.network.SkyNetwork;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;

@Mod("skyblockbuilder")
public class SkyblockBuilder extends ModXRegistration {

    private static SkyblockBuilder instance;
    private static SkyNetwork network;
    private static boolean oldConfig;
    public static final Gson PRETTY_GSON = Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setLenient();
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public SkyblockBuilder() {
        super("skyblockbuilder", null);
        oldConfig = Files.exists(SkyPaths.MOD_CONFIG.resolve("config.toml")); // remove 1.17
        instance = this;
        network = new SkyNetwork(this);

        SkyPaths.createDirectories();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG, "skyblockbuilder/config.toml");
        ConfigHandler.loadConfig(ConfigHandler.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("skyblockbuilder/config.toml"));

        ConfigParser.checkConfig();

        ConfigManager.registerConfig(new ResourceLocation("skyblockbuilder", "common-config"), LibXConfigHandler.class, false);
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.register();
        }

        Registration.registerCodecs();
        SkyPaths.generateDefaultFiles();
        TemplateLoader.loadSchematic();
        ConfigParser.deleteOldConfigFile(); // remove 1.17
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        ClientSetup.clientSetup();
    }

    public static SkyblockBuilder getInstance() {
        return instance;
    }

    public static SkyNetwork getNetwork() {
        return network;
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static boolean oldConfigExists() {
        return oldConfig;
    }
}
