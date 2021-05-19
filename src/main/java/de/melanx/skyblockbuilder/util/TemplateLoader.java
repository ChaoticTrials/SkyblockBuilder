package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
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
    private static final Map<String, Template> TEMPLATES = new HashMap<>();
    private static Template TEMPLATE = new Template();
    private static final List<BlockPos> SPAWNS = new ArrayList<>();

    public static void loadSchematic() {
        try {
            File schematic = new File(SCHEMATIC_FILE.toUri());
            CompoundNBT nbt = CompressedStreamTools.readCompressed(new FileInputStream(schematic));
            TEMPLATE.read(nbt);
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
                    int posX = JSONUtils.getInt(positions.get(0), "x");
                    int posY = JSONUtils.getInt(positions.get(1), "y");
                    int posZ = JSONUtils.getInt(positions.get(2), "z");

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

            //noinspection ConstantConditions
            for (File file : SkyPaths.TEMPLATES_DIR.toFile().listFiles()) {
                if (!file.getName().endsWith(".nbt")) {
                    continue;
                }
                CompoundNBT nbt;
                try {
                    nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
                } catch (ZipException e) {
                    continue;
                }
                Template template = new Template();
                template.read(nbt);
                TEMPLATES.put(file.getName(), template);
            }

            File schematic = new File(SCHEMATIC_FILE.toUri());
            CompoundNBT nbt = CompressedStreamTools.readCompressed(new FileInputStream(schematic));
            Template defaultTemplate = new Template();
            defaultTemplate.read(nbt);
            TEMPLATES.put("template.nbt", defaultTemplate);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load templates.", e);
        }
    }

    public static Map<String, Template> getTemplates() {
        return TEMPLATES;
    }

    public static void setTemplate(Template template) {
        TEMPLATE = template;
    }

    public static Template getTemplate() {
        return TEMPLATE;
    }

    public static List<BlockPos> getSpawns() {
        return SPAWNS;
    }
}
