package de.melanx.skyblockbuilder.compat;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.CadmusConfig;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.data.Team;
import earth.terrarium.cadmus.api.claims.admin.flags.ComponentFlag;
import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;

public class CadmusCompat {

    public static final String MODID = "cadmus";

    public static void protectSpawn(ServerLevel level, Team spawnTeam) {
        if (!CadmusConfig.protectSpawnChunks) {
            return;
        }

        String id = SkyblockBuilder.getInstance().modid + "_spawn";
        //noinspection UnstableApiUsage
        AdminClaimHandler.create(level.getServer(), id, new HashMap<>());
        //noinspection UnstableApiUsage
        AdminClaimHandler.setFlag(level.getServer(), id, "display-name", new ComponentFlag(Component.literal("SkyblockBuilder Spawn")));

        ChunkPos pos = level.getChunk(spawnTeam.getIsland().getCenter()).getPos();
        for (int xOffset = -SpawnConfig.spawnProtectionRadius; xOffset <= SpawnConfig.spawnProtectionRadius; xOffset++) {
            for (int zOffset = -SpawnConfig.spawnProtectionRadius; zOffset <= SpawnConfig.spawnProtectionRadius; zOffset++) {
                ClaimHandler.claim(level, "a:" + id, new ChunkPos(pos.x + xOffset, pos.z + zOffset), ClaimType.CLAIMED);
            }
        }
    }
}
