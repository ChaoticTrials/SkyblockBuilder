package de.melanx.skyblockbuilder.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TemplatePreview {

    private static final Set<String> LOGGED_IDS = new HashSet<>();
    private final Icon icon;
    private final Path renderingTemplatePath;
    private final ConfiguredTemplate template;
    private final String id;
    private StructureTemplate loadedRenderingTemplate;

    public TemplatePreview(ConfiguredTemplate template) {
        this.id = Util.sanitizeName(template.getName().toLowerCase(Locale.ROOT), Identifier::validPathChar);
        this.icon = TemplatePreview.searchForIconFile(this.id);
        this.renderingTemplatePath = TemplatePreview.searchForTemplateFile(this.id);
        this.template = template;

        if (this.icon == null && this.renderingTemplatePath == null && !LOGGED_IDS.contains(this.id)) {
            LOGGED_IDS.add(this.id);
            SkyblockBuilder.getLogger().info("No preview file \"{}.[png|nbt|snbt]\" found for \"{}\", using original template.", this.id, this.template.getName());
        }
    }

    private static Icon searchForIconFile(String id) {
        Path pngPath = SkyPaths.ICONS_DIR.resolve(id + ".png");

        return Files.exists(pngPath) ? new Icon(SkyblockBuilder.getInstance().id(id + "/icon"), pngPath) : null;
    }

    private static Path searchForTemplateFile(String id) {
        Path nbtPath = SkyPaths.ICONS_DIR.resolve(id + ".nbt");
        Path snbtPath = SkyPaths.ICONS_DIR.resolve(id + ".snbt");

        return Files.exists(nbtPath) ? nbtPath : Files.exists(snbtPath) ? snbtPath : null;
    }

    public PreviewType getType() {
        return this.icon != null ? PreviewType.IMAGE : this.renderingTemplatePath != null ? PreviewType.TEMPLATE : PreviewType.NONE;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public StructureTemplate getRenderingTemplatePath() {
        if (this.getType() != PreviewType.TEMPLATE) {
            return this.template.getTemplate();
        }

        if (this.loadedRenderingTemplate != null) {
            return this.loadedRenderingTemplate;
        }

        StructureTemplate structureTemplate = new StructureTemplate();

        try {
            CompoundTag nbt = TemplateUtil.readTemplate(this.renderingTemplatePath);
            structureTemplate.load(BuiltInRegistries.BLOCK, nbt);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException("Expected file at " + this.id + ".[s]nbt", e);
        }

        this.loadedRenderingTemplate = structureTemplate;

        return structureTemplate;
    }

    public record Icon(Identifier location, Path path) {}

    public enum PreviewType {
        IMAGE,
        TEMPLATE,
        NONE
    }
}
