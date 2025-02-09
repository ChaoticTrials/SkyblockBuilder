package de.melanx.skyblockbuilder.compat.heracles;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.common.HeraclesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SpreadPredicate {

    public static final Codec<SpreadPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(Codec.STRING.listOf(), Codec.STRING).xmap(
                    either -> either.map(
                            spreads -> spreads,
                            Collections::singletonList
                    ),
                    spreads -> spreads.size() == 1 ?
                            Either.right(spreads.getFirst()) :
                            Either.left(spreads)
            ).fieldOf("spreads").forGetter(ins -> ins.spreads)
    ).apply(instance, SpreadPredicate::create));

    public static final SpreadPredicate ALWAYS_TRUE = new SpreadPredicate(Collections.emptyList());
    private final List<String> spreads;

    private SpreadPredicate(List<String> spreads) {
        this.spreads = spreads;
    }

    public static SpreadPredicate create(String spread) {
        return SpreadPredicate.create(Collections.singletonList(spread));
    }

    public static SpreadPredicate create(List<String> spreads) {
        spreads = spreads.stream().filter(s -> !s.isEmpty()).toList();

        if (spreads.isEmpty()) {
            return ALWAYS_TRUE;
        }

        return new SpreadPredicate(spreads);
    }

    public boolean matches(ServerPlayer player) {
        if (this == ALWAYS_TRUE) {
            return true;
        }

        ServerLevel level = player.serverLevel();
        Team team = SkyblockSavedData.get(level).getTeamFromPlayer(player);

        for (String spread : this.spreads) {
            if (this.matches(level, team, spread, player.getX(), player.getY(), player.getZ())) {
                return true;
            }
        }

        return false;
    }

    public boolean matches(ServerLevel level, Team team, String spread, double x, double y, double z) {
        if (this == ALWAYS_TRUE) {
            return true;
        }

        if (team == null || team.isSpawn() || team.getPlacedSpreads().isEmpty()) {
            return HeraclesConfig.skipNonExistingSpreads;
        }

        Set<Team.PlacedSpread> placedSpreads = team.getPlacedSpreads(spread);
        if (WorldUtil.getConfiguredLevel(level.getServer()) != level) {
            return false;
        }

        for (Team.PlacedSpread placedSpread : placedSpreads) {
            if (BoundingBox.fromCorners(placedSpread.pos(), placedSpread.pos().offset(placedSpread.size())).isInside((int) x, (int) y, (int) z)) {
                return true;
            }
        }

        return false;
    }

    public List<String> getSpreads() {
        return List.copyOf(this.spreads);
    }
}
