package de.melanx.skyblockbuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public class ModBlockTags {

    public static final Tag.Named<Block> ADDITIONAL_VALID_SPAWN = BlockTags.bind(new ResourceLocation(SkyblockBuilder.getInstance().modid, "additional_valid_spawn").toString());
}
