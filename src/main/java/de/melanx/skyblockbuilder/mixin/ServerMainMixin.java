package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.util.fix.FixUtil;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(Main.class)
public class ServerMainMixin {

    @Redirect(
            method = "main",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;hasWorldData()Z")
    )
    private static boolean fixLevelDat(LevelStorageSource.LevelStorageAccess instance) throws IOException {
        boolean hasWorldData = instance.hasWorldData();
        if (hasWorldData) {
            FixUtil.fixBrokenLevelDat(instance);
        }

        return hasWorldData;
    }
}
