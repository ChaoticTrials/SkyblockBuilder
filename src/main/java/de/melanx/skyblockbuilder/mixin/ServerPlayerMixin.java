package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.compat.infiniverse.InfiniverseCompat;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @ModifyVariable(
            method = "changeDimension(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private DimensionTransition changeDimension(DimensionTransition transition) {
        if (!InfiniverseCompat.useInfiniverse()) {
            return transition;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;
        ResourceKey<Level> dimension = transition.newLevel().dimension();

        ResourceKey<Level> resolvedDimension = WorldUtil.resolvePortalDestination(player, player.level(), dimension);

        if (dimension == resolvedDimension) {
            return transition;
        }

        ServerLevel resolvedLevel = player.level().getServer().getLevel(resolvedDimension);
        if (resolvedLevel == null) {
            return transition;
        }

        return new DimensionTransition(
                resolvedLevel,
                transition.pos(),
                transition.speed(),
                transition.yRot(),
                transition.xRot(),
                transition.missingRespawnBlock(),
                transition.postDimensionTransition()
        );
    }
}
