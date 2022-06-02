package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class TemplateLoader {

    private static final List<String> TEMPLATE_NAMES = new ArrayList<>();
    private static final Map<String, ConfiguredTemplate> TEMPLATE_MAP = new HashMap<>();
    private static ConfiguredTemplate TEMPLATE;

    public static void updateTemplates() {
        try {
            TEMPLATE_NAMES.clear();
            TEMPLATE_MAP.clear();
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
                ConfiguredTemplate template = new ConfiguredTemplate(info);
                TEMPLATE_NAMES.add(info.name());
                TEMPLATE_MAP.put(info.name().toLowerCase(Locale.ROOT), template);
            }

            if (TEMPLATE_MAP.size() == 0) {
                throw new IllegalStateException("You need at least one configured template.");
            }

            if (TEMPLATE == null) {
                TEMPLATE = TEMPLATE_MAP.get(TEMPLATE_NAMES.get(0).toLowerCase(Locale.ROOT));
            } else {
                TEMPLATE = TemplateLoader.getConfiguredTemplate(TEMPLATE.getName());
            }
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
        List<ConfiguredTemplate> templates = new ArrayList<>(TEMPLATE_MAP.values());
        templates.sort(Comparator.comparing(ConfiguredTemplate::getName));
        return templates;
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
        return TEMPLATE_MAP.get(name.toLowerCase(Locale.ROOT));
    }

    public static ConfiguredTemplate getConfiguredTemplate() {
        return TEMPLATE;
    }

    public static Set<BlockPos> getCurrentSpawns() {
        return TEMPLATE.getDefaultSpawns();
    }
}
