package de.melanx.skyblockbuilder.util;

import com.google.gson.*;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class TemplateLoader {
    private static final ResourceLocation ID = new ResourceLocation(SkyblockBuilder.MODID, "template.nbt");
    private static final ResourceLocation SPAWNS_ID = new ResourceLocation(SkyblockBuilder.MODID, "spawns.json");
    private static final Path SCHEMATIC_FILE = FMLPaths.CONFIGDIR.get().resolve("template.nbt");
    private static final Path SPAWNS_FILE = FMLPaths.CONFIGDIR.get().resolve("spawns.json");
    public static final Template TEMPLATE = new Template();
    public static final Set<BlockPos> SPAWNS = new HashSet<>();

    public static void loadSchematic(IResourceManager manager) throws IOException {
        SPAWNS.clear();

        File schematic = new File(SCHEMATIC_FILE.toUri());
        CompoundNBT nbt;
        if (schematic.exists()) {
            nbt = CompressedStreamTools.readCompressed(new FileInputStream(schematic));
        } else {
            IResource resource = manager.getResource(ID);
            nbt = CompressedStreamTools.readCompressed(resource.getInputStream());
        }

        TEMPLATE.read(nbt);

        if (!setSpawnsFromFile()) {
            IResource resource = manager.getResource(SPAWNS_ID);
            JsonParser parser = new JsonParser();

            String s = IOUtils.toString(new InputStreamReader(resource.getInputStream()));
            JsonElement obj = parser.parse(s);
            if (!(obj instanceof JsonObject)) {
                throw new JsonSyntaxException("Spawns need to be in an json object.");
            }

            if (((JsonObject) obj).has("islandSpawns")) {
                JsonArray array = ((JsonObject) obj).getAsJsonArray("islandSpawns");
                for (JsonElement element : array) {
                    if (!(element instanceof JsonArray)) {
                        throw new JsonSyntaxException("Positions need to be written in a json array.");
                    }

                    JsonArray positions = (JsonArray) element;
                    int posX = JSONUtils.getInt(positions.get(0), "x");
                    int posY = JSONUtils.getInt(positions.get(1), "y");
                    int posZ = JSONUtils.getInt(positions.get(2), "z");

                    SPAWNS.add(new BlockPos(posX, posY, posZ));
                }
            }
        }
    }

    private static boolean setSpawnsFromFile() throws IOException {
        File spawns = new File(SPAWNS_FILE.toUri());
        if (spawns.exists()) {
            JsonParser parser = new JsonParser();

            String s = IOUtils.toString(new InputStreamReader(new FileInputStream(spawns)));
            JsonElement obj = parser.parse(s);
            if (!(obj instanceof JsonObject)) {
                SkyblockBuilder.LOGGER.error("Spawns need to be in an json object.");
                return false;
            }

            if (((JsonObject) obj).has("islandSpawns")) {
                JsonArray array = ((JsonObject) obj).getAsJsonArray("islandSpawns");
                for (JsonElement element : array) {
                    if (!(element instanceof JsonArray)) {
                        SkyblockBuilder.LOGGER.error("Positions need to be written in a json array.");
                        return false;
                    }

                    JsonArray positions = (JsonArray) element;
                    int posX = JSONUtils.getInt(positions.get(0), "x");
                    int posY = JSONUtils.getInt(positions.get(1), "y");
                    int posZ = JSONUtils.getInt(positions.get(2), "z");

                    SPAWNS.add(new BlockPos(posX, posY, posZ));
                }
            }

            return true;
        }

        return false;
    }
}
