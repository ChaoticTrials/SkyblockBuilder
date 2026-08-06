package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.compat.infiniverse.InfiniverseCompat;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @ModifyVariable(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private TeleportTransition teleport(TeleportTransition transition) {
        if (!InfiniverseCompat.useInfiniverse()) {
            return transition;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;
        ResourceKey<Level> dimension = transition.newLevel().dimension();

        ResourceKey<Level> resolvedDimension = WorldUtil.resolveTeleportDestination(player, dimension, transition.position());

        if (dimension == resolvedDimension) {
            return transition;
        }

        ServerLevel resolvedLevel = player.level().getServer().getLevel(resolvedDimension);
        if (resolvedLevel == null) {
            return transition;
        }

        return new TeleportTransition(
                resolvedLevel,
                transition.position(),
                transition.deltaMovement(),
                transition.yRot(),
                transition.xRot(),
                transition.missingRespawnBlock(),
                transition.asPassenger(),
                transition.relatives(),
                transition.postTeleportTransition()
        );
    }
}
