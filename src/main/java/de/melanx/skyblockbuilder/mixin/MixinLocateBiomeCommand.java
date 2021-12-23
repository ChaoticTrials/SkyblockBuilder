package de.melanx.skyblockbuilder.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.LocateBiomeCommand;
import net.minecraft.command.impl.LocateCommand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocateBiomeCommand.class)
public class MixinLocateBiomeCommand {

    @Inject(
            method = "func_241049_a_",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void findBiomeInSkyblock(CommandSource source, ResourceLocation biomeLocation, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ChunkGenerator chunkGenerator = world.getChunkProvider().getChunkGenerator();
        if (WorldUtil.isSkyblock(world)) {
            Registry<Biome> biomes;
            if (chunkGenerator instanceof SkyblockOverworldChunkGenerator) {
                biomes = ((SkyblockBiomeProvider) chunkGenerator.getBiomeProvider()).lookupRegistry;
            } else if (chunkGenerator instanceof SkyblockNetherChunkGenerator) {
                biomes = ((SkyblockNetherBiomeProvider) chunkGenerator.getBiomeProvider()).lookupRegistry;
            } else {
                biomes = ((SkyblockEndBiomeProvider) chunkGenerator.getBiomeProvider()).lookupRegistry;
            }
            Biome biome = biomes.getOptional(biomeLocation).orElseThrow(() -> LocateBiomeCommand.field_241044_a_.create(biomeLocation));
            BlockPos sourcePos = new BlockPos(source.getPos());
            BlockPos biomePos = source.getWorld().getBiomeLocation(biome, sourcePos, 6400, 8);
            if (biomePos == null) {
                throw LocateBiomeCommand.field_241045_b_.create(biomeLocation.toString());
            } else {
                int i = LocateCommand.func_241054_a_(source, biomeLocation.toString(), sourcePos, biomePos, "commands.locatebiome.success");
                cir.setReturnValue(i);
            }
        }
    }
}
