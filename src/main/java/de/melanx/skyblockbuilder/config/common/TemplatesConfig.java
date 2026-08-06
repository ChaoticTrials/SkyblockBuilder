package de.melanx.skyblockbuilder.config.common;

import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.values.TemplateSpawns;
import de.melanx.skyblockbuilder.config.values.TemplateSpreads;
import de.melanx.skyblockbuilder.config.values.TemplateSurroundingBlocks;
import de.melanx.skyblockbuilder.config.values.providers.SpawnsProvider;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import org.moddingx.libx.annotation.api.Codecs;
import org.moddingx.libx.annotation.codec.PrimaryConstructor;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RegisterConfig("templates")
public class TemplatesConfig {

    @Config({"The template which will be used for spawn only",
            "Example: ",
            "{",
            "    \"name\": \"default\",",
            "    \"desc\": \"\",",
            "    \"file\": \"default.nbt\",",
            "    \"spawns\": \"default\",",
            "    \"offset\": [ 0, 0, 0 ],",
            "    \"surroundingBlocks\": \"default\"",
            "}"})
    public static Optional<TemplateInfo> mainSpawnIsland = Optional.empty();

    @Config("The list of templates being available. The first entry is the default template.")
    public static List<TemplateInfo> templateList = List.of(new TemplateInfo("default", "default.nbt", new SpawnsProvider.Reference("default"), BlockPos.ZERO));

    @Config({"A list of possible spawn points.",
            "You may also directly use these in the template config, or use a reference created here."})
    public static Map<String, TemplateSpawns> spawnPointReferences = Map.of(
            "default", new TemplateSpawns(Set.of(new BlockPos(6, 3, 5)), Set.of(), Set.of(), Set.of())
    );

    @Config({"A list of blocks which can be used to surround islands/caves.",
            "You may also directly use these in the template config, or use a reference created here."})
    public static Map<String, TemplateSurroundingBlocks> surroundingBlockReferences = Map.of("default", TemplateSurroundingBlocks.EMPTY);

    @Config({"A list of file names for templates which should spread around an island",
            "You may also directly use these in the template config, or use a reference created here.",
            "Instead of \"min\" and \"max\" with same values, you could also just use a single array.",
            "\"origin\" defines from where the offset will be used. Possible values are \"zero\" and \"center\", where \"center\" is default.",
            "Example: ",
            "{",
            "    \"file\": \"default.nbt\",",
            "    \"offset\": {",
            "        \"min\": [ -6, 3, 5 ],",
            "        \"max\": [ 4, 10, 3 ]",
            "    },",
            "    \"origin\": \"center\"",
            "}"})
    public static Map<String, TemplateSpreads> spreadReferences = Map.of("default", TemplateSpreads.EMPTY);

    @Config({"A list of spreads to place around the nether portal on a team's first nether visit.",
            "Requires to_nether.[s]nbt to be configured.",
            "Uses the same format as a direct spreads definition (a JSON array of spread entries).",
            "Example: ",
            "{",
            "    \"file\": \"nether_island.nbt\",",
            "    \"offset\": [ 0, 8, 0 ],",
            "    \"origin\": \"center\"",
            "}"})
    public static TemplateSpreads netherSpreads = TemplateSpreads.EMPTY;

    @Config({"The default offset from 0, 0 to generate the islands",
            "Can be used to generate them in the middle of .mca files",
            "This applies on top of the \"offset\" defined in each template"})
    public static int defaultOffset = 0;

    @PrimaryConstructor
    public record Spawn(BlockPos pos, WorldUtil.SpawnDirection direction) {

        public static final Codec<Spawn> CODEC = Codecs.get(SkyblockBuilder.class, Spawn.class);
    }
}
