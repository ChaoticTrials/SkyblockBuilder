//package de.melanx.skyblockbuilder.mixin;
//
//import de.melanx.skyblockbuilder.util.RandomUtility;
//import net.minecraft.core.IdMap;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.chunk.ChunkBiomeContainer;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(ChunkBiomeContainer.class)
//public class MixinBiomeContainer {
//
//    @Redirect(
//            method = "Lnet/minecraft/world/biome/BiomeContainer;getBiomeIds()[I",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/util/IObjectIntIterable;getId(Ljava/lang/Object;)I"
//            )
//    )
//    public int getId(IdMap<Biome> lookup, Object value) {
//        // Should never fail because of generics
//        Biome biome = (Biome) value;
//        int id = lookup.getId(biome);
//        if (id >= 0) {
//            return id;
//        } else {
//            return RandomUtility.validateBiome(biome);
//        }
//    }
//}
