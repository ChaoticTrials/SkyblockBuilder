package de.melanx.skyblockbuilder.registration;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {

    public static final TagKey<Block> ADDITIONAL_VALID_SPAWN = BlockTags.create(SkyblockBuilder.getInstance().resource("additional_valid_spawn"));
    public static final TagKey<Block> PREVENT_SCHEDULED_TICK = BlockTags.create(SkyblockBuilder.getInstance().resource("prevent_scheduled_tick"));
}
