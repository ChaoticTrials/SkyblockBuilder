package de.melanx.skyblockbuilder.template;

import com.google.common.collect.ImmutableMap;
import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TemplateLoader {

    private static final List<ConfiguredTemplate> TEMPLATES = new ArrayList<>();
    private static final ConfiguredTemplate DEFAULT_TEMPLATE = new ConfiguredTemplate(new TemplateInfo("default", "default.nbt", "default"));
    private static ConfiguredTemplate TEMPLATE = new ConfiguredTemplate(new TemplateInfo("default", "default.nbt", "default"));

    public static void updateTemplates() {
        try {
            TEMPLATES.clear();
            SkyPaths.copyTemplateFile();

            for (TemplateInfo info : TemplateConfig.templates) {
                TEMPLATES.add(new ConfiguredTemplate(info));
            }
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
