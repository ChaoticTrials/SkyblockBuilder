package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.util.fix.FixUtil;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {

    @Inject(method = "openWorldLoadLevelData", at = @At("HEAD"))
    public void openWorldLoadLevelData(LevelStorageSource.LevelStorageAccess levelStorage, Runnable onFail, CallbackInfo ci) throws IOException {
        FixUtil.fixBrokenLevelDat(levelStorage);
    }
}
