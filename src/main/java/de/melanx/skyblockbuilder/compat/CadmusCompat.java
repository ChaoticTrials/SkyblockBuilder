package de.melanx.skyblockbuilder.compat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.CadmusConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class CadmusCompat {

    public static final String MODID = "cadmus";

    public static LiteralArgumentBuilder<CommandSourceStack> spawnProtectionCommand() {
        return Commands.literal(MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal(SkyblockBuilder.getInstance().modid)
                        .then(Commands.literal("protectSpawn")
                                .executes(source -> {
                                    ServerLevel level = source.getSource().getLevel();
                                    SkyblockSavedData data = SkyblockSavedData.get(level);
                                    CadmusCompat.protectSpawn(level, data.getSpawn(), true);
                                    source.getSource().sendSuccess(() -> Component.translatable("cadmus.skyblockbuilder.claim_spawn"), true);
                                    return 1;
                                })));
    }

    public static void protectSpawn(ServerLevel level, Team spawnTeam) {
        CadmusCompat.protectSpawn(level, spawnTeam, false);
    }

    public static void protectSpawn(ServerLevel level, Team spawnTeam, boolean forced) {
        if (!CadmusConfig.protectSpawnChunks && !forced) {
            return;
        }

//        String id = SkyblockBuilder.getInstance().modid + "_spawn";
//        //noinspection UnstableApiUsage
//        AdminClaimHandler.create(level.getServer(), id, new HashMap<>());
//        //noinspection UnstableApiUsage
//        AdminClaimHandler.setFlag(level.getServer(), id, "display-name", new ComponentFlag(CadmusConfig.displayName));
//
//        ChunkPos pos = level.getChunk(spawnTeam.getIsland().getCenter()).getPos();
//        for (int xOffset = -SpawnConfig.spawnProtectionRadius; xOffset <= SpawnConfig.spawnProtectionRadius; xOffset++) {
//            for (int zOffset = -SpawnConfig.spawnProtectionRadius; zOffset <= SpawnConfig.spawnProtectionRadius; zOffset++) {
//                ClaimHandler.claim(level, "a:" + id, new ChunkPos(pos.x + xOffset, pos.z + zOffset), ClaimType.CLAIMED);
//            }
//        }
    }
}
