package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DumpUtil {

    public static final int MANIFEST_VERSION = 2;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private static final Map<String, IModInfo> MOD_INFO_MAP = ModList.get().getMods().stream().collect(Collectors.toMap(IModInfo::getModId, info -> info));
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Set<String> HASH_SKIP_MODS = Set.of("minecraft", "forge");

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
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, CommonComponents.GUI_OPEN_IN_BROWSER))
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

            JsonObject modHashes = new JsonObject();
            JsonObject sbHashes = DumpUtil.computeModHashes(MOD_INFO_MAP.get(SkyblockBuilder.getInstance().modid));
            if (sbHashes != null) {
                modHashes.add(SkyblockBuilder.getInstance().modid, sbHashes);
            }

            for (IModInfo.ModVersion dependency : MOD_INFO_MAP.get(SkyblockBuilder.getInstance().modid).getDependencies()) {
                String depId = dependency.getModId();
                if (HASH_SKIP_MODS.contains(depId)) {
                    continue;
                }

                IModInfo modInfo = MOD_INFO_MAP.get(depId);
                if (modInfo != null) {
                    JsonObject hashes = DumpUtil.computeModHashes(modInfo);
                    if (hashes != null) {
                        modHashes.add(depId, hashes);
                    }
                }
            }

            JsonArray filesArray = new JsonArray();
            if (includeConfigs) {
                DumpUtil.addDirToZip(filesArray, SkyPaths.MOD_CONFIG, zipStream, Paths.get("config"), false);

                Map<ResourceLocation, String> diffs = DumpUtil.configDiffs();
                for (Map.Entry<ResourceLocation, String> entry : diffs.entrySet()) {
                    ResourceLocation key = entry.getKey();
                    String value = entry.getValue();

                    Path filePath = Paths.get("config", "changed_values", key.getPath() + ".diff");
                    DumpUtil.addStringToZip(filesArray, zipStream, value, filePath);
                }
            }

            if (includeTemplates) {
                DumpUtil.addDirToZip(filesArray, SkyPaths.TEMPLATES_DIR, zipStream, true);
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
                        DumpUtil.addCensoredFileToZip(filesArray, zipStream, latestLog, Paths.get("logs", "latest.log"));
                    }

                    Path debugLog = FMLPaths.GAMEDIR.get().resolve("logs").resolve("debug.log");
                    if (debugLog.toFile().exists()) {
                        DumpUtil.addCensoredFileToZip(filesArray, zipStream, debugLog, Paths.get("logs", "debug.log"));
                    }
                }

                if (includeCrashReport) {
                    Optional<Path> crashReportOptional = DumpUtil.findLatestCrashReport();
                    if (crashReportOptional.isPresent()) {
                        DumpUtil.addCensoredFileToZip(filesArray, zipStream, crashReportOptional.get(), Paths.get("logs", "crash-report.txt"));
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
            manifest.add("hashes", modHashes);
            manifest.add("files", filesArray);
            DumpUtil.addStringToZip(filesArray, zipStream, SkyblockBuilder.PRETTY_GSON.toJson(manifest), Paths.get("manifest.json"));
        } catch (IOException e) {
            SkyblockBuilder.getLogger().error("Failed to create zip file", e);
        }

        return file;
    }

    private static void addDirToZip(JsonArray fileCollector, Path dirPath, ZipOutputStream zipStream, boolean recursive) throws IOException {
        DumpUtil.addDirToZip(fileCollector, dirPath, zipStream, dirPath.getFileName(), recursive);
    }

    private static void addDirToZip(JsonArray fileCollector, Path dirPath, ZipOutputStream zipStream, Path parentFolder, boolean recursive) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    Path zipEntryName = parentFolder.resolve(path.getFileName());
                    DumpUtil.addFileToZip(fileCollector, zipStream, path, zipEntryName);
                } else if (recursive) {
                    DumpUtil.addDirToZip(fileCollector, path, zipStream, parentFolder.resolve(path.getFileName()), recursive);
                }
            }
        }
    }

    private static void addFileToZip(JsonArray fileCollector, ZipOutputStream zipStream, Path filePath) throws IOException {
        DumpUtil.addFileToZip(fileCollector, zipStream, filePath, filePath.getFileName());
    }

    private static void addFileToZip(JsonArray fileCollector, ZipOutputStream zipStream, Path filePath, Path zipEntryPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            // force ZIP-standard forward‐slashes because of Windows
            String name = zipEntryPath.toString().replace(File.separatorChar, '/');
            ZipEntry zipEntry = new ZipEntry(name);
            zipStream.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];

            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                zipStream.write(buffer, 0, len);
            }
            zipStream.closeEntry();

            JsonObject fileData = new JsonObject();
            fileData.addProperty("name", zipEntryPath.getFileName().toString());
            fileData.addProperty("path", name);
            fileCollector.add(fileData);
        }
    }

    private static void addStringToZip(JsonArray fileCollector, ZipOutputStream zipStream, String content, Path zipEntryPath) throws IOException {
        // force ZIP-standard forward‐slashes because of Windows
        String name = zipEntryPath.toString().replace(File.separatorChar, '/');
        ZipEntry zipEntry = new ZipEntry(name);
        zipStream.putNextEntry(zipEntry);
        zipStream.write(content.getBytes());
        zipStream.closeEntry();

        JsonObject fileData = new JsonObject();
        fileData.addProperty("name", zipEntryPath.getFileName().toString());
        fileData.addProperty("path", name);
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
            if (!id.getNamespace().equals(SkyblockBuilder.getInstance().modid)) {
                continue;
            }

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

            String defaultContent = "{\n" + DumpUtil.applyIndent(defaultState.writeObject(changedValues, config.groups, 0)) + "\n}\n";
            String currentContent = "{\n" + DumpUtil.applyIndent(currentState.writeObject(changedValues, config.groups, 0)) + "\n}\n";
            String diff = DumpUtil.computeUnifiedDiff(defaultContent, currentContent, "default/" + id.getPath(), "current/" + id.getPath());

            // Skip if the serialized output is identical despite value-level inequality (e.g., ResourceList quirks)
            if (!DumpUtil.hasActualChanges(diff)) {
                continue;
            }

            configDiffs.put(id, diff);
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

    @Nullable
    private static JsonObject computeModHashes(IModInfo modInfo) {
        IModFileInfo fileInfo = modInfo.getOwningFile();
        if (fileInfo instanceof ModFileInfo modFileInfo) {
            Path jarPath = modFileInfo.getFile().getFilePath();
            if (Files.isRegularFile(jarPath)) {
                try {
                    byte[] bytes = Files.readAllBytes(jarPath);
                    JsonObject hashes = new JsonObject();
                    hashes.addProperty("md5", DumpUtil.hashBytes(bytes, "MD5"));
                    hashes.addProperty("sha1", DumpUtil.hashBytes(bytes, "SHA-1"));
                    hashes.addProperty("sha512", DumpUtil.hashBytes(bytes, "SHA-512"));
                    return hashes;
                } catch (IOException | NoSuchAlgorithmException e) {
                    SkyblockBuilder.getLogger().warn("Failed to compute hashes for {}", modInfo.getModId(), e);
                }
            }
        }

        return null;
    }

    private static String hashBytes(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hashBytes = digest.digest(bytes);
        StringBuilder sb = new StringBuilder(hashBytes.length * 2);

        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static void addCensoredFileToZip(JsonArray fileCollector, ZipOutputStream zipStream, Path filePath, Path zipEntryPath) throws IOException {
        String content = Files.readString(filePath);
        DumpUtil.addStringToZip(fileCollector, zipStream, IP_PATTERN.matcher(content).replaceAll("[REDACTED]"), zipEntryPath);
    }

    private static String computeUnifiedDiff(String oldContent, String newContent, String oldLabel, String newLabel) {
        String[] oldLines = oldContent.split("\n", -1);
        String[] newLines = newContent.split("\n", -1);
        int m = oldLines.length;
        int n = newLines.length;

        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (oldLines[i - 1].equals(newLines[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        List<String> diffLines = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldLines[i - 1].equals(newLines[j - 1])) {
                diffLines.add(0, " " + oldLines[i - 1]);
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                diffLines.add(0, "+" + newLines[j - 1]);
                j--;
            } else {
                diffLines.add(0, "-" + oldLines[i - 1]);
                i--;
            }
        }

        return "--- " + oldLabel + "\n+++ " + newLabel + "\n" + String.join("\n", diffLines) + "\n";
    }

    // Returns true if the diff (past its two header lines) contains at least one + or - line
    private static boolean hasActualChanges(String diff) {
        int firstNewline = diff.indexOf('\n');
        int secondNewline = firstNewline < 0 ? -1 : diff.indexOf('\n', firstNewline + 1);

        String body = secondNewline < 0 ? "" : diff.substring(secondNewline + 1);
        for (String line : body.split("\n", -1)) {
            if (line.startsWith("+") || line.startsWith("-")) {
                return true;
            }
        }

        return false;
    }

    private static String applyIndent(String input) {
        return "  " + input.replace("\n", "\n" + "  ");
    }
}
