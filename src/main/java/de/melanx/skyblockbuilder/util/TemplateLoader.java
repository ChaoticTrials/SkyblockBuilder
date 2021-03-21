package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TemplateLoader {
    
    private static final Path SCHEMATIC_FILE = FMLPaths.CONFIGDIR.get().resolve(SkyblockBuilder.MODID).resolve("template.nbt");
    private static final Path SPAWNS_FILE = FMLPaths.CONFIGDIR.get().resolve(SkyblockBuilder.MODID).resolve("spawns.json");
    public static final Template TEMPLATE = new Template();
    public static final List<BlockPos> SPAWNS = new ArrayList<>();

    public static void loadSchematic(IResourceManager manager) throws IOException {
        File schematic = new File(SCHEMATIC_FILE.toUri());
        CompoundNBT nbt = CompressedStreamTools.readCompressed(new FileInputStream(schematic));
        TEMPLATE.read(nbt);

        SPAWNS.clear();
        File spawns = new File(SPAWNS_FILE.toUri());
        JsonParser parser = new JsonParser();

        String s = IOUtils.toString(new InputStreamReader(new FileInputStream(spawns)));
        JsonElement obj = parser.parse(s);
        if (!(obj instanceof JsonObject)) {
            throw new IllegalStateException("Spawns need to be in an json object.");
        }

        if (((JsonObject) obj).has("islandSpawns")) {
            JsonArray array = ((JsonObject) obj).getAsJsonArray("islandSpawns");
            for (JsonElement element : array) {
                if (!(element instanceof JsonArray)) {
                    throw new IllegalStateException("Positions need to be written in a json array.");
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
