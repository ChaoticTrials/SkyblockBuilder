package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {

    @Inject(
            method = "findNearestBiome",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void findBiomeSimplified(Biome biome, BlockPos pos, int radius, int increment, CallbackInfoReturnable<BlockPos> cir) {
        ServerLevel level = (ServerLevel) (Object) this;
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        if (generator instanceof SkyblockNoiseBasedChunkGenerator) {
            BlockPos ret = generator.getBiomeSource().findBiomeHorizontal(pos.getX(), pos.getY(), pos.getZ(), radius, increment, (target) -> {
                return Objects.equals(target.getRegistryName(), biome.getRegistryName());
            }, level.random, true, generator.climateSampler());
            cir.setReturnValue(ret);
        }
    }
}
