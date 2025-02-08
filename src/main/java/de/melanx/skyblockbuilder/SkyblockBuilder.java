package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.melanx.skyblockbuilder.client.ClientEventListener;
import de.melanx.skyblockbuilder.compat.heracles.HeraclesCompat;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.datagen.BlockStatesProvider;
import de.melanx.skyblockbuilder.datagen.ItemModelProvider;
import de.melanx.skyblockbuilder.datagen.ModTagProvider;
import de.melanx.skyblockbuilder.datagen.WorldPresetProvider;
import de.melanx.skyblockbuilder.network.SkyNetwork;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.Util;
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

    private static SkyblockBuilder instance;
    private final SkyNetwork network;
    private final Logger logger;
    public static final Gson PRETTY_GSON = Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setLenient();
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public SkyblockBuilder(IEventBus bus, Dist dist) {
        instance = this;
        this.network = new SkyNetwork(this);
        this.logger = LoggerFactory.getLogger(SkyblockBuilder.class);

        SkyPaths.createDirectories();
        NeoForge.EVENT_BUS.register(new SpawnProtectionEvents());

        if (dist == Dist.CLIENT) {
            bus.register(new ClientEventListener());
        }

        DatagenSystem.create(this, system -> {
            system.addRegistryProvider(WorldPresetProvider::new);
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
        return instance.logger;
    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        // NO-OP
    }
}
