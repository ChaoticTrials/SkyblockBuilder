package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.DimensionsConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.providers.SpawnsProvider;
import de.melanx.skyblockbuilder.config.values.providers.SpreadsProvider;
import de.melanx.skyblockbuilder.config.values.providers.SurroundingBlocksProvider;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class TemplateLoader {

    public static final StructurePlaceSettings STRUCTURE_PLACE_SETTINGS = new StructurePlaceSettings().setKnownShape(true).setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
    private static final List<String> TEMPLATE_NAMES = new ArrayList<>();
    private static final Map<String, ConfiguredTemplate> TEMPLATE_MAP = new HashMap<>();
    private static ConfiguredTemplate TEMPLATE;
    private static Optional<Integer> PALETTE_INDEX = Optional.empty();
    private static NetherPortalTemplate NETHER_PORTAL;

    public static void updateTemplates() {
        try {
            TEMPLATE_NAMES.clear();
            TEMPLATE_MAP.clear();
            NETHER_PORTAL = null;
            SkyPaths.copyTemplateFile();
            Set<String> takenNames = new HashSet<>();

            for (TemplateInfo info : TemplatesConfig.templates) {
                if (info.spawns() instanceof SpawnsProvider.Reference(String name) && !TemplatesConfig.spawns.containsKey(name)) {
                    throw new IllegalArgumentException("Spawns configuration \"" + info.spawns() + "\" is not defined: " + info.name());
                }

                if (info.spawns().templateSpawns().allEmpty()) {
                    throw new IllegalArgumentException("Spawns configuration \"" + info.spawns() + "\" is empty: " + info.name());
                }

                if (info.surroundingBlocks() instanceof SurroundingBlocksProvider.Reference(String name) && !TemplatesConfig.surroundingBlocks.containsKey(name)) {
                    throw new IllegalArgumentException("Surrounding blocks configuration \"" + info.surroundingBlocks() + "\" is not defined: " + info.name());
                }

                if (info.spreads() instanceof SpreadsProvider.Reference(String name) && !TemplatesConfig.spreads.containsKey(name)) {
                    throw new IllegalArgumentException("Spreads configuration \"" + info.spreads() + "\" is not defined: " + info.name());
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
                SkyblockBuilder.getLogger().info("Loaded template \"{}\" from \"{}\".", info.name(), info.file());
            }

            if (TEMPLATE_MAP.isEmpty()) {
                throw new IllegalStateException("You need at least one configured template.");
            }

            if (TEMPLATE == null) {
                TEMPLATE = TEMPLATE_MAP.get(TEMPLATE_NAMES.getFirst().toLowerCase(Locale.ROOT));
            } else {
                TEMPLATE = TemplateLoader.getConfiguredTemplate(TEMPLATE.getName(), false);
            }

            DimensionsConfig.Nether.netherPortalStructure.ifPresent(filePath -> NETHER_PORTAL = new NetherPortalTemplate(filePath));
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
        TemplateLoader.setTemplate(template, Optional.empty());
    }

    public static void setTemplate(ConfiguredTemplate template, Optional<Integer> paletteIndex) {
        TEMPLATE = template;
        PALETTE_INDEX = paletteIndex;
    }

    public static StructureTemplate getTemplate() {
        if (TEMPLATE == null) {
            throw new IllegalStateException("Tried to access template before set.");
        }

        return TEMPLATE.getTemplate();
    }

    @Nullable
    public static ConfiguredTemplate getConfiguredTemplate(String name) {
        return TemplateLoader.getConfiguredTemplate(name, true);
    }

    @Nullable
    public static ConfiguredTemplate getConfiguredTemplate(String name, boolean randomPalette) {
        ConfiguredTemplate configuredTemplate = TEMPLATE_MAP.get(name.toLowerCase(Locale.ROOT));
        if (!randomPalette && PALETTE_INDEX.isPresent()) {
            configuredTemplate = configuredTemplate.onlyWithPalette(PALETTE_INDEX.get());
        }

        return configuredTemplate;
    }

    public static ConfiguredTemplate getConfiguredTemplate() {
        return TEMPLATE;
    }

    public static NetherPortalTemplate getNetherPortalTemplate() {
        return NETHER_PORTAL;
    }

    public static Set<TemplatesConfig.Spawn> getCurrentSpawns() {
        return TEMPLATE.getDefaultSpawns();
    }
}
