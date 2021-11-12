package de.melanx.skyblockbuilder.template;

import com.google.common.collect.ImmutableMap;
import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class TemplateLoader {

    private static final List<ConfiguredTemplate> TEMPLATES = new ArrayList<>();
    private static final ConfiguredTemplate DEFAULT_TEMPLATE = new ConfiguredTemplate(new TemplateInfo("default", "default.nbt", "default"));
    private static ConfiguredTemplate TEMPLATE = new ConfiguredTemplate(new TemplateInfo("default", "default.nbt", "default"));

    public static void updateTemplates() {
        try {
            TEMPLATES.clear();
            SkyPaths.copyTemplateFile();
            Set<String> takenNames = new HashSet<>();

            for (TemplateInfo info : TemplateConfig.templates) {
                if (!TemplateConfig.spawns.containsKey(info.spawns())) {
                    throw new IllegalArgumentException("Spawn configuration \"" + info.spawns() + "\" is not defined: " + info.name());
                }

                if (!SkyPaths.TEMPLATES_DIR.resolve(info.file()).toFile().exists()) {
                    throw new IllegalArgumentException("Template file \"" + info.file() + "\" does not exist: " + info.name());
                }

                if (takenNames.contains(info.name().toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Template name \"" + info.name() + "\" is defined too many times.");
                }

                takenNames.add(info.name().toLowerCase(Locale.ROOT));
                TEMPLATES.add(new ConfiguredTemplate(info));
            }

            if (TEMPLATES.size() == 0) {
                throw new IllegalStateException("You need at least one configured template.");
            }

            TEMPLATE = TEMPLATES.get(0);

            TEMPLATES.sort(Comparator.comparing(ConfiguredTemplate::getName));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load templates.", e);
        }
    }

    public static Map<String, StructureTemplate> getTemplates() {
        //noinspection UnstableApiUsage
        return TEMPLATES.stream().collect(ImmutableMap.toImmutableMap(ConfiguredTemplate::getName, ConfiguredTemplate::getTemplate));
    }

    public static List<ConfiguredTemplate> getConfiguredTemplates() {
        return TEMPLATES;
    }

    public static void setTemplate(ConfiguredTemplate template) {
        TEMPLATE = template;
    }

    public static StructureTemplate getTemplate() {
        return TEMPLATE.getTemplate();
    }

    @Nullable
    public static ConfiguredTemplate getConfiguredTemplate(String name) {
        for (ConfiguredTemplate template : TEMPLATES) {
            if (template.getName().equalsIgnoreCase(name)) {
                return template;
            }
        }

        return null;
    }

    public static ConfiguredTemplate getConfiguredTemplate() {
        return TEMPLATE;
    }

    public static Set<BlockPos> getCurrentSpawns() {
        return TEMPLATE.getDefaultSpawns();
    }

    public static Set<BlockPos> getDefaultSpawns() {
        return DEFAULT_TEMPLATE.getDefaultSpawns();
    }
}
