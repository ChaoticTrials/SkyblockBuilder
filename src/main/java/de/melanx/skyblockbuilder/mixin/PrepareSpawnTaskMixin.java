package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.compat.infiniverse.InfiniverseCompat;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.config.PrepareSpawnTask;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrepareSpawnTask.class)
public class PrepareSpawnTaskMixin {

    @Shadow @Final private NameAndId nameAndId;

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "start", at = @At("HEAD"))
    private void skyblockbuilder$start(CallbackInfo ci) {
        ServerLevel overworld = this.server.overworld();
        if (!InfiniverseCompat.useInfiniverse() || !WorldUtil.isSkyblock(overworld)) {
            return;
        }

        SkyblockSavedData data = SkyblockSavedData.get(overworld);
        if (data == null) {
            return;
        }

        Team team = data.getTeamFromPlayer(this.nameAndId.id());
        if (team == null) {
            return;
        }

        SkyblockSavedData.restoreTeamDimensions(data, team);
        team.setLastSeen(overworld.getGameTime());
    }
}
