package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public class SpawnsCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("spawns")
                .executes(context -> SpawnsCommand.showSpawns(context.getSource()));
    }

    @SuppressWarnings("SameReturnValue")
    private static int showSpawns(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team = null;
        if (source.getEntity() instanceof ServerPlayer player) {
            team = data.getTeamFromPlayer(player);
        }

        if (team == null) {
            team = data.getSpawn();
        }

        if (team.isSpawn() && !PermissionManager.INSTANCE.mayExecuteOpCommand(source)) {
            source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return 0;
        }

        Set<TemplatesConfig.Spawn> spawns = team.getPossibleSpawns();
        if (!spawns.isEmpty()) {
            source.sendSystemMessage(SkyComponents.INFO_SHOW_TEAM_SPAWNS.apply(team.getName()));
        }

        for (TemplatesConfig.Spawn spawn : spawns) {
            BlockPos pos = spawn.pos();
            source.sendSystemMessage(Component.literal(" - ").append(RandomUtility.getFormattedPos(pos)));
            if (source.getEntity() instanceof ServerPlayer player) {
                level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
            } else {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
            }
        }

        return 1;
    }
}
