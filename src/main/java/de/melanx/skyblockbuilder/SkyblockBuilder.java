package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.melanx.skyblockbuilder.client.ClientSetup;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.network.SkyNetwork;
import de.melanx.skyblockbuilder.util.SkyPaths;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.util.ResourceList;
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

@Mod("skyblockbuilder")
public class SkyblockBuilder extends ModXRegistration {

    private static SkyblockBuilder instance;
    private static SkyNetwork network;
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
        network = new SkyNetwork(this);

        SkyPaths.createDirectories();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG, "skyblockbuilder/config.toml");
        ConfigHandler.loadConfig(ConfigHandler.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("skyblockbuilder/config.toml"));

        // TODO 1.17: remove as fast as possible
        // start config override
        LibXConfigHandler.Structures.generationStructures = new ResourceList(!ConfigHandler.toggleWhitelist.get(), b -> {
            for (String s : ConfigHandler.whitelistStructures.get()) {
                b.simple(new ResourceLocation(s));
            }
        });
        LibXConfigHandler.Structures.generationFeatures = new ResourceList(!ConfigHandler.toggleWhitelist.get(), b -> {
            for (String s : ConfigHandler.whitelistFeatures.get()) {
                b.simple(new ResourceLocation(s));
            }
        });

        LibXConfigHandler.Dimensions.Nether.Default = ConfigHandler.defaultNether.get();
        LibXConfigHandler.Dimensions.End.Default = ConfigHandler.defaultEnd.get();
        LibXConfigHandler.Dimensions.End.mainIsland = ConfigHandler.defaultEndIsland.get();

        LibXConfigHandler.World.surface = ConfigHandler.generateSurface.get();
        LibXConfigHandler.World.surfaceSettings = ConfigHandler.generationSettings.get();
        LibXConfigHandler.World.seaHeight = ConfigHandler.seaHeight.get();
        LibXConfigHandler.World.islandDistance = ConfigHandler.islandDistance.get();
        LibXConfigHandler.World.biomeRange = ConfigHandler.biomeRange.get();
        LibXConfigHandler.World.SingleBiome.biome = new ResourceLocation(ConfigHandler.biome.get());
        LibXConfigHandler.World.SingleBiome.enabled = ConfigHandler.singleBiome.get();

        LibXConfigHandler.Spawn.radius = ConfigHandler.spawnRadius.get();
        LibXConfigHandler.Spawn.dimension = new ResourceLocation(ConfigHandler.spawnDimension.get());
        LibXConfigHandler.Spawn.direction = ConfigHandler.direction.get();
        LibXConfigHandler.Spawn.height = ConfigHandler.generationHeight.get();

        LibXConfigHandler.Inventory.clearInv = ConfigHandler.clearInv.get();
        LibXConfigHandler.Inventory.dropItems = ConfigHandler.dropItems.get();

        LibXConfigHandler.Utility.selfManage = ConfigHandler.selfManageTeam.get();
        LibXConfigHandler.Utility.createOwnTeam = ConfigHandler.createOwnTeam.get();
        LibXConfigHandler.Utility.Teleports.spawn = ConfigHandler.spawnTeleport.get();
        LibXConfigHandler.Utility.Teleports.allowVisits = ConfigHandler.allowVisits.get();
        LibXConfigHandler.Utility.Teleports.home = ConfigHandler.homeEnabled.get();
        LibXConfigHandler.Utility.Spawns.range = ConfigHandler.modifySpawnRange.get();
        LibXConfigHandler.Utility.Spawns.modifySpawns = ConfigHandler.modifySpawns.get();
        // enc config override

        ConfigManager.registerConfig(new ResourceLocation("skyblockbuilder", "common-config"), LibXConfigHandler.class, false);
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.register();
        }

        Registration.registerCodecs();
        SkyPaths.generateDefaultFiles();
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
}
