package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {

    @Inject(method = "inPortalDimension", at = @At(value = "RETURN"), cancellable = true)
    private static void inPortalDimension(Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && WorldUtil.isTeamPortalDimension(level)) {
            cir.setReturnValue(true);
        }
    }
}
