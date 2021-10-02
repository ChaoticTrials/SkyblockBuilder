package de.melanx.skyblockbuilder;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class ModBlockTags {

    public static final ITag.INamedTag<Block> ADDITIONAL_VALID_SPAWN = BlockTags.makeWrapperTag(new ResourceLocation(SkyblockBuilder.getInstance().modid, "additional_valid_spawn").toString());
}
