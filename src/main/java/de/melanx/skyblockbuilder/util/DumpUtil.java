package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.moddingx.libx.config.ConfigManager;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigKey;
import org.moddingx.libx.impl.config.ConfigState;

import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DumpUtil {

    public static int MANIFEST_VERSION = 1;
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private static final Map<String, IModInfo> MOD_INFO_MAP = ModList.get().getMods().stream().collect(Collectors.toMap(IModInfo::getModId, info -> info));

    public static Component getIssueUrl() {
        IModInfo modInfo = MOD_INFO_MAP.get(SkyblockBuilder.getInstance().modid);
        IModFileInfo owningFile = modInfo.getOwningFile();
        String url;
        if (owningFile instanceof ModFileInfo info) {
            url = info.getIssueURL().toString();
        } else {
            url = "https://www.github.com/ChaoticTrials/SkyblockBuilder";
        }

        return Component.literal(url).withStyle(Style.EMPTY
                .applyFormats(ChatFormatting.BLUE, ChatFormatting.UNDERLINE)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url + "/new?template=dump_bug_report.yml"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.link.open")))
        );
    }

    public static Path createZip(boolean includeConfigs, boolean includeTemplates, boolean includeLevelDat, boolean includeLog, boolean includeCrashReport, boolean includeSkyblockBuilderWorldData) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        Path file = SkyPaths.DUMPS.resolve(DATE_FORMAT.format(new Date()) + ".zip");
        try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            zipStream.setLevel(Deflater.BEST_COMPRESSION);

            JsonObject settings = new JsonObject();
            settings.addProperty("configs", includeConfigs);
            settings.addProperty("templates", includeTemplates);
            settings.addProperty("level_dat", includeLevelDat);
            settings.addProperty("log", includeLog);
            settings.addProperty("crash_report", includeCrashReport);
            settings.addProperty("world_data", includeSkyblockBuilderWorldData);

            JsonObject modVersions = new JsonObject();
            modVersions.addProperty(SkyblockBuilder.getInstance().modid, MOD_INFO_MAP.get(SkyblockBuilder.getInstance().modid).getVersion().toString());
            for (IModInfo.ModVersion dependency : MOD_INFO_MAP.get(SkyblockBuilder.getInstance().modid).getDependencies()) {
                IModInfo modInfo = MOD_INFO_MAP.get(dependency.getModId());
                if (modInfo != null) {
                    modVersions.addProperty(dependency.getModId(), modInfo.getVersion().toString());
                }
            }

            JsonArray filesArray = new JsonArray();
            if (includeConfigs) {
                DumpUtil.addDirToZip(filesArray, SkyPaths.MOD_CONFIG, zipStream, Paths.get("config"));

                Map<ResourceLocation, String> diffs = DumpUtil.configDiffs();
                for (Map.Entry<ResourceLocation, String> entry : diffs.entrySet()) {
                    ResourceLocation key = entry.getKey();
                    String value = entry.getValue();

                    Path filePath = Paths.get("config", "changed_values", key.getPath() + ".json5");
                    DumpUtil.addStringToZip(filesArray, zipStream, value, filePath);
                }
            }

            if (includeTemplates) {
                DumpUtil.addDirToZip(filesArray, SkyPaths.TEMPLATES_DIR, zipStream);
            }

            if (server != null) {
                server.storageSource.checkLock();
                Path levelPath = server.storageSource.getWorldDir().resolve(server.storageSource.getLevelId()).toRealPath();
                if (includeLevelDat) {
                    Path levelDat = server.storageSource.getLevelPath(LevelResource.LEVEL_DATA_FILE);
                    if (levelDat.toFile().exists()) {
                        DumpUtil.addFileToZip(filesArray, zipStream, levelDat);
                    }
                }

                if (includeLog) {
                    Path latestLog = FMLPaths.GAMEDIR.get().resolve("logs").resolve("latest.log");
                    if (latestLog.toFile().exists()) {
                        DumpUtil.addFileToZip(filesArray, zipStream, latestLog, Paths.get("logs", "latest.log"));
                    }
                }

                if (includeCrashReport) {
                    Optional<Path> crashReportOptional = DumpUtil.findLatestCrashReport();
                    if (crashReportOptional.isPresent()) {
                        DumpUtil.addFileToZip(filesArray, zipStream, crashReportOptional.get(), Paths.get("logs", "crash-report.txt"));
                    }
                }

                if (includeSkyblockBuilderWorldData) {
                    Path data = levelPath.resolve("data").resolve("skyblock_builder.dat");
                    if (data.toFile().exists()) {
                        DumpUtil.addFileToZip(filesArray, zipStream, data, Paths.get("data", "skyblock_builder.dat"));
                    }
                }
            }

            JsonObject manifest = new JsonObject();
            manifest.addProperty("manifest_version", MANIFEST_VERSION);
            manifest.addProperty("manifest_id", UUID.randomUUID().toString());
            manifest.add("settings", settings);
            manifest.add("versions", modVersions);
            manifest.add("files", filesArray);
            DumpUtil.addStringToZip(filesArray, zipStream, SkyblockBuilder.PRETTY_GSON.toJson(manifest), Paths.get("manifest.json"));
        } catch (IOException e) {
            SkyblockBuilder.getLogger().error("Failed to create zip file", e);
        }

        return file;
    }

    private static void addDirToZip(JsonArray fileCollector, Path dirPath, ZipOutputStream zipStream) throws IOException {
        DumpUtil.addDirToZip(fileCollector, dirPath, zipStream, dirPath.getFileName());
    }

    private static void addDirToZip(JsonArray fileCollector, Path dirPath, ZipOutputStream zipStream, Path parentFolder) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    Path zipEntryName = parentFolder.resolve(path.getFileName());
                    DumpUtil.addFileToZip(fileCollector, zipStream, path, zipEntryName);
                }
            }
        }
    }

    private static void addFileToZip(JsonArray fileCollector, ZipOutputStream zipStream, Path filePath) throws IOException {
        DumpUtil.addFileToZip(fileCollector, zipStream, filePath, filePath.getFileName());
    }

    private static void addFileToZip(JsonArray fileCollector, ZipOutputStream zipStream, Path filePath, Path zipEntryPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            ZipEntry zipEntry = new ZipEntry(zipEntryPath.toString());
            zipStream.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];

            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                zipStream.write(buffer, 0, len);
            }
            zipStream.closeEntry();

            JsonObject fileData = new JsonObject();
            fileData.addProperty("name", zipEntryPath.getFileName().toString());
            fileData.addProperty("path", zipEntryPath.toString().replace('\\', '/'));
            fileCollector.add(fileData);
        }
    }

    private static void addStringToZip(JsonArray fileCollector, ZipOutputStream zipStream, String content, Path zipEntryPath) throws IOException {
        ZipEntry zipEntry = new ZipEntry(zipEntryPath.toString());
        zipStream.putNextEntry(zipEntry);
        zipStream.write(content.getBytes());
        zipStream.closeEntry();

        JsonObject fileData = new JsonObject();
        fileData.addProperty("name", zipEntryPath.getFileName().toString());
        fileData.addProperty("path", zipEntryPath.toString().replace('\\', '/'));
        fileCollector.add(fileData);
    }

    private static Optional<Path> findLatestCrashReport() {
        Path crashReports = FMLPaths.GAMEDIR.get().resolve("crash-reports");
        if (!Files.exists(crashReports) || !Files.isDirectory(crashReports)) {
            return Optional.empty();
        }

        try (Stream<Path> paths = Files.list(crashReports)) {
            return paths.filter(Files::isRegularFile)
                    .max(Comparator.comparingLong(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis();
                        } catch (IOException e) {
                            return 0;
                        }
                    }));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Map<ResourceLocation, String> configDiffs() {
        Map<ResourceLocation, String> configDiffs = new HashMap<>();
        for (ResourceLocation id : ConfigManager.configs()) {
            if (id.getNamespace().equals(SkyblockBuilder.getInstance().modid)) {


                ConfigImpl config = ConfigImpl.getConfig(id);
                ConfigState currentState = config.stateFromValues();
                ConfigState defaultState = DumpUtil.getDefaultConfigState(config);
                if (defaultState == null) {
                    continue;
                }

                Set<ConfigKey> changedValues = new HashSet<>();
                for (ConfigKey configKey : config.keys.values()) {
                    if (!currentState.getValue(configKey).equals(defaultState.getValue(configKey))) {
                        changedValues.add(configKey);
                    }
                }

                if (changedValues.isEmpty()) {
                    continue;
                }

                String configDiff = "{\n" + DumpUtil.applyIndent(currentState.writeObject(changedValues, config.groups, 0)) + "\n}\n";
                configDiffs.put(id, configDiff);
            }
        }

        return configDiffs;
    }

    @Nullable
    private static ConfigState getDefaultConfigState(ConfigImpl instance) {
        try {
            Field defaultStateField = ConfigImpl.class.getDeclaredField("defaultState");
            defaultStateField.setAccessible(true);
            return (ConfigState) defaultStateField.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private static String applyIndent(String input) {
        return "  " + input.replace("\n", "\n" + "  ");
    }
}
