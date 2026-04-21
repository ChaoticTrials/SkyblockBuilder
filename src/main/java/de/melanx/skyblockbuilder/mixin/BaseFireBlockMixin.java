package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {

    @Inject(method = "inPortalDimension", at = @At(value = "RETURN"), cancellable = true)
    private static void test(Level level, CallbackInfoReturnable<Boolean> cir) {
        if (level.dimension().location().getNamespace().equals(SkyblockBuilder.getInstance().modid)) {
            cir.setReturnValue(true);
        }
    }
}
