package de.melanx.skyblockbuilder.config;

import de.melanx.skyblockbuilder.template.TemplateInfo;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterConfig;
import io.github.noeppi_noeppi.libx.config.Config;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

@RegisterConfig("templates")
public class TemplateConfig {

    @Config
    public static List<TemplateInfo> templates = List.of(new TemplateInfo("default", "default.nbt", "default"));

    @Config
    public static Map<String, List<BlockPos>> spawns = Map.of("default", List.of(
            new BlockPos(6, 3, 5)
    ));
}
