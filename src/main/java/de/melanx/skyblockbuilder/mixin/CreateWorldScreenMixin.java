package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.config.common.ClientConfig;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @ModifyVariable(
            method = "onCreate",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private boolean modifyFlag(boolean original) {
        return original || ClientConfig.disableExperimentalWarning;
    }
}
