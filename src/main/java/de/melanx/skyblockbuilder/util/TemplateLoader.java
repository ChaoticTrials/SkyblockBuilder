package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

public class TemplateLoader {

    private static final Path SCHEMATIC_FILE = SkyPaths.MOD_CONFIG.resolve("template.nbt");
    private static final Path SPAWNS_FILE = SkyPaths.MOD_CONFIG.resolve("spawns.json");
    private static final Map<String, StructureTemplate> TEMPLATES = new HashMap<>();
    private static StructureTemplate TEMPLATE = new StructureTemplate();
    private static final List<BlockPos> SPAWNS = new ArrayList<>();

    public static void loadSchematic() {
        try {
            File schematic = new File(SCHEMATIC_FILE.toUri());
            CompoundTag nbt = NbtIo.readCompressed(new FileInputStream(schematic));
            TEMPLATE.load(nbt);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load template", e);
        }

        try {
            SPAWNS.clear();
            File spawns = new File(SPAWNS_FILE.toUri());
            JsonParser parser = new JsonParser();

            String s = IOUtils.toString(new InputStreamReader(new FileInputStream(spawns)));
            JsonElement obj = parser.parse(s);
            if (!(obj instanceof JsonObject)) {
                throw new IllegalStateException("Spawns need to be in a json object.");
            }

            if (((JsonObject) obj).has("islandSpawns")) {
                JsonArray array = ((JsonObject) obj).getAsJsonArray("islandSpawns");
                for (JsonElement element : array) {
                    if (!(element instanceof JsonArray)) {
                        throw new IllegalStateException("Positions need to be written in a json array.");
                    }

                    JsonArray positions = (JsonArray) element;
                    int posX = GsonHelper.convertToInt(positions.get(0), "x");
                    int posY = GsonHelper.convertToInt(positions.get(1), "y");
                    int posZ = GsonHelper.convertToInt(positions.get(2), "z");

                    SPAWNS.add(new BlockPos(posX, posY, posZ));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load spawns", e);
        }
    }

    public static void updateTemplates() {
        try {
            TEMPLATES.clear();
            SkyPaths.copyTemplateFile();

            //noinspection ConstantConditions
            for (File file : SkyPaths.TEMPLATES_DIR.toFile().listFiles()) {
                if (!file.getName().endsWith(".nbt")) {
                    continue;
                }
                CompoundTag nbt;
                try {
                    nbt = NbtIo.readCompressed(new FileInputStream(file));
                } catch (ZipException e) {
                    continue;
                }
                StructureTemplate template = new StructureTemplate();
                template.load(nbt);
                TEMPLATES.put(file.getName(), template);
            }

            File schematic = new File(SCHEMATIC_FILE.toUri());
            CompoundTag nbt = NbtIo.readCompressed(new FileInputStream(schematic));
            StructureTemplate defaultTemplate = new StructureTemplate();
            defaultTemplate.load(nbt);
            TEMPLATES.put("template.nbt", defaultTemplate);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load templates.", e);
        }
    }

    public static Map<String, StructureTemplate> getTemplates() {
        return TEMPLATES;
    }

    public static void setTemplate(StructureTemplate template) {
        TEMPLATE = template;
    }

    public static StructureTemplate getTemplate() {
        return TEMPLATE;
    }

    public static List<BlockPos> getSpawns() {
        return SPAWNS;
    }
}
