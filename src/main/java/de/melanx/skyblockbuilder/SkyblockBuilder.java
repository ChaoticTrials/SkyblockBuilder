package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import de.melanx.skyblockbuilder.client.ClientEventListener;
import de.melanx.skyblockbuilder.compat.heracles.HeraclesCompat;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.datagen.*;
import de.melanx.skyblockbuilder.network.SkyNetwork;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.world.presets.BiomeParametersPreset;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = "skyblockbuilder")
public final class SkyblockBuilder extends ModXRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkyblockBuilder.class);
    private static SkyblockBuilder instance;
    private final SkyNetwork network;
    public static final Gson PRETTY_GSON = Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setStrictness(Strictness.LENIENT);
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public SkyblockBuilder(IEventBus bus, Dist dist) {
        instance = this;
        this.network = new SkyNetwork(this);

        SkyPaths.createDirectories();
        NeoForge.EVENT_BUS.register(new SpawnProtectionEvents());

        if (dist == Dist.CLIENT) {
            bus.register(new ClientEventListener());
        }

        DatagenSystem.create(this, system -> {
            system.addRegistryProvider(WorldPresetProvider::new);
            system.addRegistryProvider(SkyblockBiomeParameters::new);
            system.addDataProvider(ItemModelProvider::new);
            system.addDataProvider(ModTagProvider::new);
            system.addDataProvider(BlockStatesProvider::new);
        });
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.register();
        }

        if (ModList.get().isLoaded(HeraclesCompat.MODID)) {
            HeraclesCompat.registerHeracles();
        }

        TemplateLoader.updateTemplates();
        SkyPaths.generateDefaultFiles(null);

        if (PermissionsConfig.forceSkyblockCheck) {
            SkyblockBuilder.getLogger().warn("'forceSkyblockCheck' is enabled");
        }

        MultiNoiseBiomeSourceParameterList.Preset.BY_NAME.put(BiomeParametersPreset.FILTERED_OVERWORLD.id(), BiomeParametersPreset.FILTERED_OVERWORLD);
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded(HeraclesCompat.MODID)) {
//            HeraclesCompat.registerHeraclesClient(); todo re-add Heracles
        }
    }

    public static SkyblockBuilder getInstance() {
        return instance;
    }

    public static SkyNetwork getNetwork() {
        return instance.network;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        // NO-OP
    }
}
