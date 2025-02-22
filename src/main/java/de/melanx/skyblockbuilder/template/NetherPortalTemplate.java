package de.melanx.skyblockbuilder.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class NetherPortalTemplate {
    private final StructureTemplate structure = new StructureTemplate();
    private final BlockPos portalOffset;

    public NetherPortalTemplate(File file) {
        // todo config for custom portal overworld -> nether || nether -> overworld
        try {
            this.structure.load(BuiltInRegistries.BLOCK.asLookup(), TemplateUtil.readTemplate(file.toPath()));
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        this.portalOffset = NetherPortalTemplate.calcPortalOffset(this.structure);
    }

    private static BlockPos calcPortalOffset(StructureTemplate template) {
        Set<StructureTemplate.StructureBlockInfo> netherPortals = new HashSet<>();
        for (StructureTemplate.Palette palette : template.palettes) {
            for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                if (blockInfo.state().is(Blocks.NETHER_PORTAL)) {
                    netherPortals.add(blockInfo);
                }
            }
        }

        if (netherPortals.isEmpty()) {
            throw new RuntimeException("There is no nether portal in this template");
        }

        return netherPortals.stream().sorted(Comparator.comparing(blockInfo -> blockInfo.pos().getY())).toList().getFirst().pos();
    }

    public StructureTemplate getStructure() {
        return this.structure;
    }

    public BlockPos getPortalOffset() {
        return this.portalOffset.multiply(-1);
    }
}
