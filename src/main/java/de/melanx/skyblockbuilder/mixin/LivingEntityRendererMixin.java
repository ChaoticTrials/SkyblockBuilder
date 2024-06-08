package de.melanx.skyblockbuilder.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void shouldShowName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player == null) {
            cir.setReturnValue(false);
        }
    }
}
