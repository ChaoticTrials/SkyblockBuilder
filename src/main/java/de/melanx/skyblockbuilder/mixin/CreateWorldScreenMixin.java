package de.melanx.skyblockbuilder.mixin;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.config.common.ClientConfig;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Redirect(
            method = "onCreate",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/Lifecycle;add(Lcom/mojang/serialization/Lifecycle;)Lcom/mojang/serialization/Lifecycle;"
            )
    )
    private Lifecycle isExperimental(Lifecycle instance, Lifecycle other) {
        if (ClientConfig.disableExperimentalWarning) {
            return Lifecycle.stable();
        }

        return instance.add(other);
    }
}
