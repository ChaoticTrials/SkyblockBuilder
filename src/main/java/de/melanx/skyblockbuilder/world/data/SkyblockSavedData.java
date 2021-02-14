package de.melanx.skyblockbuilder.world.data;

import de.melanx.skyblockbuilder.util.Registration;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/*
 * Credits go to Botania authors
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
public class SkyblockSavedData extends WorldSavedData {
    private static final String NAME = "skyblock_builder";

    /**
     * The offset is chosen to put islands under default settings in the center of a chunk region.
     */
    private static final int OFFSET = 1;

    public Map<IslandPos, Team> skyblocks = new HashMap<>();
    private Spiral spiral = new Spiral();

    public SkyblockSavedData() {
        super(NAME);
    }

    public static SkyblockSavedData get(ServerWorld world) {
        return world.getSavedData().getOrCreate(SkyblockSavedData::new, NAME);
    }

    public IslandPos getSpawn() {
        for (Map.Entry<IslandPos, Team> entry : this.skyblocks.entrySet()) {
            Team team = entry.getValue();
            if (team.hasPlayer(Util.DUMMY_UUID)) {
                return this.skyblocks.get(entry.getKey()).getIsland();
            }
        }
        IslandPos pos = new IslandPos(OFFSET, OFFSET);

        Set<UUID> players = new HashSet<>();
        players.add(Util.DUMMY_UUID);

        Team team = new Team(this, pos, Util.DUMMY_UUID);
        team.setPossibleSpawns(this.getPossibleSpawns(pos));
        team.setPlayers(players);

        this.skyblocks.put(pos, team);
        this.markDirty();
        return pos;
    }

    public Pair<IslandPos, Team> create(UUID playerId) {
        int scale = 8;
        IslandPos islandPos;
        do {
            int[] pos = this.spiral.next();
            islandPos = new IslandPos(pos[0] * scale + OFFSET, pos[1] * scale + OFFSET);
        } while (this.skyblocks.containsKey(islandPos));

        Set<UUID> players = new HashSet<>();
        players.add(playerId);

        Set<BlockPos> positions = getPossibleSpawns(islandPos.getCenter());

        Team team = new Team(this, islandPos, playerId);
        team.setPossibleSpawns(positions);
        team.setPlayers(players);

        this.skyblocks.put(islandPos, team);

        this.markDirty();
        return Pair.of(islandPos, team);
    }

    @Override
    public void read(CompoundNBT nbt) {
        Map<IslandPos, Team> map = new HashMap<>();
        for (INBT inbt : nbt.getList("Islands", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT tag = (CompoundNBT) inbt;

            IslandPos island = IslandPos.fromTag(tag);
            Team team = new Team(this, island, Util.DUMMY_UUID);
            team.deserializeNBT(tag);

            map.put(island, team);
        }
        this.skyblocks = map;
        this.spiral = Spiral.fromArray(nbt.getIntArray("SpiralState"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT islands = new ListNBT();
        for (Map.Entry<IslandPos, Team> entry : this.skyblocks.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            islands.add(entry.getKey().toTag());
            islands.add(entry.getValue().serializeNBT());
        }

        nbt.putIntArray("SpiralState", this.spiral.toIntArray());
        nbt.put("Islands", islands);
        return nbt;
    }

    @Nullable
    public IslandPos getTeamIsland(String team) {
        for (Map.Entry<IslandPos, Team> entry : this.skyblocks.entrySet()) {
            if (entry.getValue().getName().equals(team.toLowerCase())) {
                return entry.getValue().getIsland();
            }
        }
        return null;
    }

    public boolean hasPlayerTeam(PlayerEntity player) {
        return this.hasPlayerTeam(player.getGameProfile().getId());
    }

    public boolean hasPlayerTeam(UUID player) {
        return this.getTeamFromPlayer(player) != null;
    }

    public boolean addPlayerToTeam(String teamName, PlayerEntity player) {
        return this.addPlayerToTeam(teamName.toLowerCase(), player.getGameProfile().getId());
    }

    public boolean addPlayerToTeam(String teamName, UUID player) {
        for (Team team : this.skyblocks.values()) {
            if (team.getName().equals(teamName)) {
                team.addPlayer(player);
                this.markDirty();
                return true;
            }
        }
        return false;
    }

    public boolean createTeamAndJoin(String teamName, PlayerEntity player) {
        return this.createTeamAndJoin(teamName.toLowerCase(), player.getGameProfile().getId());
    }

    public boolean createTeamAndJoin(String teamName, UUID player) {
        if (this.teamExists(teamName)) {
            return false;
        }

        Pair<IslandPos, Team> pair = this.create(player);
        Team team = pair.getRight();
        team.setName(teamName);
        this.markDirty();
        return true;
    }

    public boolean removePlayerFromTeam(PlayerEntity player) {
        return this.removePlayerFromTeam(player.getGameProfile().getId());
    }

    public boolean removePlayerFromTeam(UUID player) {
        Iterator<Map.Entry<IslandPos, Team>> itr = this.skyblocks.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<IslandPos, Team> entry = itr.next();
            Team team = entry.getValue();
            if (team.getPlayers().contains(player) && team.removePlayer(player) && team.isEmpty()) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Team getTeamFromPlayer(PlayerEntity player) {
        return this.getTeamFromPlayer(player.getGameProfile().getId());
    }

    @Nullable
    public Team getTeamFromPlayer(UUID player) {
        for (Team team : this.skyblocks.values()) {
            if (team.getPlayers().contains(player)) {
                return team;
            }
        }
        return null;
    }

    public boolean teamExists(String name) {
        name = name.toLowerCase();
        for (Team team : this.skyblocks.values()) {
            if (team.getName() == null) {
                continue;
            }

            if (team.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Collection<Team> getTeams() {
        return this.skyblocks.values();
    }

    public Set<BlockPos> getPossibleSpawns(IslandPos pos) {
        if (this.skyblocks.get(pos) == null) {
            return getPossibleSpawns(pos.getCenter());
        }

        return this.skyblocks.get(pos).getPossibleSpawns();
    }

    public static Set<BlockPos> getPossibleSpawns(BlockPos center) {
        Set<BlockPos> positions = new HashSet<>();
        for (Template.Palette info : TemplateLoader.TEMPLATE.blocks) {
            for (Template.BlockInfo blockInfo : info.func_237157_a_()) {
                if (blockInfo.state == Registration.SPAWN_BLOCK.get().getDefaultState()) {
                    positions.add(center.add(blockInfo.pos.toImmutable()));
                }
            }
        }
        return positions;
    }

    // Adapted from https://stackoverflow.com/questions/398299/looping-in-a-spiral
    private static class Spiral {
        private int x = 0;
        private int y = 0;
        private int dx = 0;
        private int dy = -1;

        Spiral() {
        }

        Spiral(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        int[] next() {
            if (this.x == this.y || this.x < 0 && this.x == -this.y || this.x > 0 && this.x == 1 - this.y) {
                int t = this.dx;
                this.dx = -this.dy;
                this.dy = t;
            }
            this.x += this.dx;
            this.y += this.dy;
            return new int[]{this.x, this.y};
        }

        int[] toIntArray() {
            return new int[]{this.x, this.y, this.dx, this.dy};
        }

        static Spiral fromArray(int[] ints) {
            return new Spiral(ints[0], ints[1], ints[2], ints[3]);
        }
    }
}
