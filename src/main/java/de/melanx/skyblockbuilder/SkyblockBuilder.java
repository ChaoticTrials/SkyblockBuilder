package de.melanx.skyblockbuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.melanx.skyblockbuilder.client.ClientSetup;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.network.SkyNetwork;
import de.melanx.skyblockbuilder.util.SkyPaths;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.RegistrationBuilder;
import net.minecraft.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("skyblockbuilder")
public final class SkyblockBuilder extends ModXRegistration {

    private static SkyblockBuilder instance;
    private static SkyNetwork network;
    private final Logger logger;
    public static final Gson PRETTY_GSON = Util.make(() -> {
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.disableHtmlEscaping();
        gsonbuilder.setLenient();
        gsonbuilder.setPrettyPrinting();
        return gsonbuilder.create();
    });

    public SkyblockBuilder() {
        super(null);
        instance = this;
        network = new SkyNetwork();
        this.logger = LoggerFactory.getLogger(SkyblockBuilder.class);

        SkyPaths.createDirectories();
        MinecraftForge.EVENT_BUS.register(new SpawnProtectionEvents());
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.register();
        }

        Registration.registerCodecs();
        SkyPaths.generateDefaultFiles(null);
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

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        builder.setVersion(1);
    }
}
