package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.blocks.SpawnBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "BLOCK")
public class ModBlocks {

    public static final Block spawnBlock = new SpawnBlock(SkyblockBuilder.getInstance(), BlockBehaviour.Properties.of().noCollission().instabreak());
}
