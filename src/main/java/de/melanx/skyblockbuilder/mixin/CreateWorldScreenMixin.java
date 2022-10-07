package de.melanx.skyblockbuilder.mixin;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

    @Inject(
            method = "onCreate",
            at = @At(value = "HEAD")
    )
    public void applyDataPacksToSkyblockPreset(CallbackInfo ci) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;

        // [Vanilla copy] CreateWorldScreen#tryApplyNewDataPacks without setting screens (except on failure)
        if (screen.worldGenSettingsComponent.preset.isPresent() && screen.worldGenSettingsComponent.preset.get().is(SkyblockBuilder.getInstance().resource("skyblock"))) {
            PackRepository packrepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource());
            List<String> selectedIds = ImmutableList.copyOf(packrepository.getSelectedIds());
            List<String> availableIds = packrepository.getAvailableIds().stream().filter(id -> !selectedIds.contains(id)).toList();
            DataPackConfig datapackconfig = new DataPackConfig(selectedIds, availableIds);
            WorldLoader.InitConfig defaultLoadConfig = CreateWorldScreen.createDefaultLoadConfig(packrepository, datapackconfig);
            WorldLoader.load(defaultLoadConfig, (resourceManager, packConfig) -> {
                WorldCreationContext creationContext = screen.worldGenSettingsComponent.settings();
                RegistryAccess registryAccess = creationContext.registryAccess();
                RegistryAccess.Writable builtinRegistryAccess = RegistryAccess.builtinCopy();
                DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
                DynamicOps<JsonElement> dynamicLoadedOps = RegistryOps.createAndLoad(JsonOps.INSTANCE, builtinRegistryAccess, resourceManager);
                DataResult<JsonElement> defaultSettings = WorldGenSettings.CODEC.encodeStart(dynamicOps, creationContext.worldGenSettings()).setLifecycle(Lifecycle.stable());
                DataResult<WorldGenSettings> loadedSettings = defaultSettings.flatMap(json -> WorldGenSettings.CODEC.parse(dynamicLoadedOps, json));
                RegistryAccess.Frozen frozenRegistryAccess = builtinRegistryAccess.freeze();
                Lifecycle lifecycle = loadedSettings.lifecycle().add(frozenRegistryAccess.allElementsLifecycle());
                WorldGenSettings worldGenSettings = loadedSettings.getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", SkyblockBuilder.getLogger()::error));
                if (frozenRegistryAccess.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).size() == 0) {
                    throw new IllegalStateException("Needs at least one world preset to continue");
                } else if (frozenRegistryAccess.registryOrThrow(Registry.BIOME_REGISTRY).size() == 0) {
                    throw new IllegalStateException("Needs at least one biome to continue");
                } else {
                    return Pair.of(Pair.of(worldGenSettings, lifecycle), frozenRegistryAccess);
                }
            }, (closeableResourceManager, serverResources, frozenRegistryAccess, settingsLifecyclePair) -> {
                closeableResourceManager.close();
                return new WorldCreationContext(settingsLifecyclePair.getFirst(), settingsLifecyclePair.getSecond(), frozenRegistryAccess, serverResources);
            }, Util.backgroundExecutor(), screen.getMinecraft()).thenAcceptAsync(worldCreationContext -> {
                screen.dataPacks = datapackconfig;
                screen.worldGenSettingsComponent.updateSettings(worldCreationContext);
                screen.rebuildWidgets();
            }, screen.getMinecraft()).handle((unused, throwable) -> {
                if (throwable != null) {
                    SkyblockBuilder.getLogger().warn("Failed to validate datapack", throwable);
                    screen.getMinecraft().tell(() -> {
                        screen.getMinecraft().setScreen(new ConfirmScreen(confirmed -> {
                            if (confirmed) {
                                screen.openDataPackSelectionScreen();
                            } else {
                                screen.dataPacks = new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of()); // FORGE: Revert to *actual* vanilla data
                                screen.getMinecraft().setScreen(screen);
                            }

                        }, Component.translatable("dataPack.validation.failed"), CommonComponents.EMPTY, Component.translatable("dataPack.validation.back"), Component.translatable("dataPack.validation.reset")));
                    });
                }

                return null;
            });
        }
    }
}
