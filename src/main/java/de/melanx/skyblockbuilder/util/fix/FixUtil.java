package de.melanx.skyblockbuilder.util.fix;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FixUtil {

    public static void fixBrokenLevelDat(LevelStorageSource.LevelStorageAccess levelStorage) throws IOException {
        Path levelDatFixedIndicator = levelStorage.getLevelDirectory().path().resolve(".skyblockbuilder_level_dat_fixed");
        if (Files.exists(levelDatFixedIndicator)) {
            SkyblockBuilder.getLogger().info("level.dat already fixed, skipping");
            return;
        }

        try {
            CompoundTag levelTag = NbtIo.readCompressed(levelStorage.getLevelDirectory().dataFile(), NbtAccounter.unlimitedHeap());
            CompoundTag data = levelTag.getCompound("Data");
            CompoundTag worldGenSettings = data.getCompound("WorldGenSettings");
            CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
            CompoundTag overworldSettings = dimensions.getCompound("minecraft:overworld");
            CompoundTag overworldChunkGeneratorSettings = overworldSettings.getCompound("generator");
            CompoundTag overworldBiomeSourceSettings = overworldChunkGeneratorSettings.getCompound("biome_source");

            CompoundTag netherSettings = dimensions.getCompound("minecraft:the_nether");
            CompoundTag netherChunkGeneratorSettings = netherSettings.getCompound("generator");
            CompoundTag netherBiomeSourceSettings = netherChunkGeneratorSettings.getCompound("biome_source");

            boolean overworldChanged = FixUtil.fixSkyBiomeSource(overworldBiomeSourceSettings);
            boolean netherChanged = FixUtil.fixSkyBiomeSource(netherBiomeSourceSettings);

            if (!overworldChanged && !netherChanged) {
                Files.writeString(levelDatFixedIndicator, "Nothing needed to be fixed. Lucky you!");
                return;
            }

            try {
                // LevelStorageSource#saveLevelData
                Path path = levelStorage.getLevelDirectory().path();
                Path latest = Files.createTempFile(path, "level", ".dat");
                NbtIo.writeCompressed(levelTag, latest);
                Path oldBackup = levelStorage.getLevelDirectory().oldDataFile();
                Path current = levelStorage.getLevelDirectory().dataFile();
                Util.safeReplaceFile(current, latest, oldBackup);
                String msg = "Converted faulty level.dat to use new biome source, please report any issues on GitHub immediately! https://github.com/ChaoticTrials/SkyblockBuilder/issues";
                SkyblockBuilder.getLogger().info(msg);
                Files.writeString(levelDatFixedIndicator, msg);
            } catch (Exception exception) {
                SkyblockBuilder.getLogger().error("Failed to convert faulty level.dat", exception);
            }
        } catch (Exception e) {
            throw new IOException("Failed to check for valid level.dat", e);
        }
    }

    private static boolean fixSkyBiomeSource(CompoundTag biomeSourceSettings) {
        CompoundTag copy = biomeSourceSettings.copy();
        String biomeSourceType = biomeSourceSettings.getString("type");
        if (biomeSourceType.equals("skyblockbuilder:sky") && biomeSourceSettings.contains("parent", CompoundTag.TAG_COMPOUND)) {
            return fixSkyBiomeSource(biomeSourceSettings.getCompound("parent"));
        }

        if (biomeSourceType.isEmpty()) {
            if (biomeSourceSettings.contains("preset") && biomeSourceSettings.getString("preset").equals("skyblockbuilder:filtered_overworld")) {
                biomeSourceSettings.putString("type", "skyblockbuilder:sky_overworld");
            } else {
                biomeSourceSettings.putString("type", "minecraft:multi_noise");
            }
        } else if (!biomeSourceType.equals("skyblockbuilder:sky_overworld")) {
            if (biomeSourceSettings.contains("preset") && biomeSourceSettings.getString("preset").equals("skyblockbuilder:filtered_overworld")) {
                biomeSourceSettings.putString("type", "skyblockbuilder:sky_overworld");
            }
        }

        return !copy.equals(biomeSourceSettings);
    }
}
