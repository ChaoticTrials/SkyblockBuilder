package de.melanx.skyblockbuilder.config.common;

import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.*;

@RegisterConfig("templates")
public class TemplatesConfig {

    @Config("The list of templates being available. The first entry is the default template.")
    public static List<TemplateInfo> templates = List.of(new TemplateInfo("default", "default.nbt", "default", new TemplateInfo.Offset(0, 0, 0)));

    @Config
    public static Map<String, Map<String, Set<BlockPos>>> spawns = Map.of("default", Map.of(
            WorldUtil.Directions.SOUTH.name().toLowerCase(Locale.ROOT), Set.of(new BlockPos(6, 3, 5)),
            WorldUtil.Directions.WEST.name().toLowerCase(Locale.ROOT), Set.of(),
            WorldUtil.Directions.NORTH.name().toLowerCase(Locale.ROOT), Set.of(),
            WorldUtil.Directions.EAST.name().toLowerCase(Locale.ROOT), Set.of()
    ));

    @Config("A list of blocks which can be used to surround islands/caves.")
    public static Map<String, List<Block>> surroundingBlocks = Map.of("default", List.of());

    @Config({"A list of file names for templates which should spread around an island",
            "Instead of \"minOffset\" and \"maxOffset\" with same values, you could also just use \"offset\".",
            "Example: ",
            "{",
            "    \"file\": \"default.nbt\",",
            "    \"minOffset\": [ -6, 3, 5 ],",
            "    \"maxOffset\": [ 4, 10, 3 ]",
            "}"})
    public static Map<String, List<TemplateInfo.SpreadInfo>> spreads = Map.of("default", List.of());

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

    public record Spawn(BlockPos pos, WorldUtil.Directions direction) {}
}
