package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class TemplateLoader {

    private static final List<ConfiguredTemplate> TEMPLATES = new ArrayList<>();
    private static final List<String> TEMPLATE_NAMES = new ArrayList<>();
    private static ConfiguredTemplate TEMPLATE;

    public static void updateTemplates() {
        try {
            TEMPLATES.clear();
            TEMPLATE_NAMES.clear();
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
                TEMPLATE_NAMES.add(info.name());
            }

            if (TEMPLATES.size() == 0) {
                throw new IllegalStateException("You need at least one configured template.");
            }

            if (TEMPLATE == null) {
                TEMPLATE = TEMPLATES.get(0);
            } else {
                TEMPLATE = TemplateLoader.getConfiguredTemplate(TEMPLATE.getName());
            }

            TEMPLATES.sort(Comparator.comparing(ConfiguredTemplate::getName));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load templates.", e);
        }
    }

    public static void updateTemplateNames(List<String> names) {
        TEMPLATE_NAMES.clear();
        TEMPLATE_NAMES.addAll(names);
    }

    public static List<String> getTemplateNames() {
        return TEMPLATE_NAMES;
    }

    /**
     * This provides a list with all the configured templates and its corresponding StructureTemplates.
     * <br>
     * Should mainly be used on server side. It's possible that there is too much data to sync all templates to the
     * client. Use {@link TemplateLoader#getTemplateNames()} on client and send a packet to server to communicate.
     */
    public static List<ConfiguredTemplate> getConfiguredTemplates() {
        return TEMPLATES;
    }

    public static void setTemplate(ConfiguredTemplate template) {
        TEMPLATE = template;
    }

    public static StructureTemplate getTemplate() {
        if (TEMPLATE == null) {
            throw new IllegalStateException("Tried to access template before set.");
        }

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
}
