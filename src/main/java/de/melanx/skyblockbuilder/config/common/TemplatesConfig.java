package de.melanx.skyblockbuilder.config.common;

import de.melanx.skyblockbuilder.config.values.TemplateSpawns;
import de.melanx.skyblockbuilder.config.values.TemplateSpreads;
import de.melanx.skyblockbuilder.config.values.TemplateSurroundingBlocks;
import de.melanx.skyblockbuilder.config.values.providers.SpawnsProvider;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RegisterConfig("templates")
public class TemplatesConfig {

    @Config("The list of templates being available. The first entry is the default template.")
    public static List<TemplateInfo> templates = List.of(new TemplateInfo("default", "default.nbt", new SpawnsProvider.Reference("default"), new TemplateInfo.Offset(0, 0, 0)));

    @Config
    public static Map<String, TemplateSpawns> spawns = Map.of(
            "default", new TemplateSpawns(Set.of(new BlockPos(6, 3, 5)), Set.of(), Set.of(), Set.of())
    );

    @Config("A list of blocks which can be used to surround islands/caves.")
    public static Map<String, TemplateSurroundingBlocks> surroundingBlocks = Map.of("default", TemplateSurroundingBlocks.EMPTY);

    @Config({"A list of file names for templates which should spread around an island",
            "Instead of \"minOffset\" and \"maxOffset\" with same values, you could also just use \"offset\".",
            "\"origin\" defines from where the offset will be used. Possible values are \"zero\" and \"center\", where \"center\" is default.",
            "Example: ",
            "{",
            "    \"file\": \"default.nbt\",",
            "    \"minOffset\": [ -6, 3, 5 ],",
            "    \"maxOffset\": [ 4, 10, 3 ],",
            "    \"origin\": \"center\"",
            "}"})
    public static Map<String, TemplateSpreads> spreads = Map.of("default", TemplateSpreads.EMPTY);

    @Config({"The default offset from 0, 0 to generate the islands",
            "Can be used to generate them in the middle of .mca files",
            "This applies on top of the \"offset\" defined in each template"})
    public static int defaultOffset = 0;

    @Config({"The template which will be used for spawn only",
            "Example: ",
            "{",
            "    \"name\": \"default\",",
            "    \"desc\": \"\",",
            "    \"file\": \"default.nbt\",",
            "    \"spawns\": \"default\",",
            "    \"offset\": [ 0, 0, 0 ],",
            "    \"surroundingMargin\": 0,",
            "    \"surroundingBlocks\": \"default\"",
            "}"})
    public static Optional<TemplateInfo> spawn = Optional.empty();

    public record Spawn(BlockPos pos, WorldUtil.SpawnDirection direction) {}
}
