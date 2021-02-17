package de.melanx.skyblockbuilder.world.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
import net.minecraft.world.storage.DimensionSavedDataManager;
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

    private static final int SPAWN = 0;

    private final ServerWorld world;
    public Map<String, Team> skyblocks = new HashMap<>();
    public BiMap<String, IslandPos> skyblockPositions = HashBiMap.create();
    private Spiral spiral = new Spiral();

    public SkyblockSavedData(ServerWorld world) {
        super(NAME);
        this.world = world;
    }

    public static SkyblockSavedData get(ServerWorld world) {
        DimensionSavedDataManager storage = world.getServer().func_241755_D_().getSavedData();
        return storage.getOrCreate(() -> new SkyblockSavedData(world), NAME);
    }

    public IslandPos getSpawn() {
        for (Team team : this.skyblocks.values()) {
            if (team.hasPlayer(Util.DUMMY_UUID)) {
                return this.skyblockPositions.get(team.getName());
            }
        }
        IslandPos pos = new IslandPos(SPAWN, SPAWN);

        Set<UUID> players = new HashSet<>();
        players.add(Util.DUMMY_UUID);

        Team team = new Team(this, pos);
        team.setPossibleSpawns(this.getPossibleSpawns(pos));
        team.setPlayers(players);
        team.setName("spawn");

        this.skyblocks.put(team.getName(), team);
        this.skyblockPositions.put(team.getName(), pos);
        this.markDirty();
        return pos;
    }

    public Pair<IslandPos, Team> create(UUID playerId) {
        IslandPos islandPos;
        do {
            int[] pos = this.spiral.next();
            islandPos = new IslandPos(pos[0] + SPAWN, pos[1] + SPAWN);
        } while (this.skyblockPositions.containsValue(islandPos));

        Set<UUID> players = new HashSet<>();
        players.add(playerId);

        Set<BlockPos> positions = getPossibleSpawns(islandPos.getCenter());

        Team team = new Team(this, islandPos);
        team.setPossibleSpawns(positions);
        team.setPlayers(players);

        this.skyblocks.put(team.getName(), team);
        this.skyblockPositions.put(team.getName(), islandPos);

        this.markDirty();
        return Pair.of(islandPos, team);
    }

    @Override
    public void read(CompoundNBT nbt) {
        Map<String, Team> skyblocks = new HashMap<>();
        BiMap<String, IslandPos> skyblockPositions = HashBiMap.create();
        for (INBT inbt : nbt.getList("Islands", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT tag = (CompoundNBT) inbt;

            IslandPos island = IslandPos.fromTag(tag.getCompound("Island"));
            Team team = new Team(this, island);
            team.deserializeNBT(tag);

            skyblocks.put(team.getName(), team);
            skyblockPositions.put(team.getName(), island);
        }
        this.skyblocks = skyblocks;
        this.skyblockPositions = skyblockPositions;
        this.spiral = Spiral.fromArray(nbt.getIntArray("SpiralState"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT islands = new ListNBT();
        for (Team team : this.skyblocks.values()) {
            if (team.isEmpty()) {
                continue;
            }
            islands.add(team.serializeNBT());
        }

        nbt.putIntArray("SpiralState", this.spiral.toIntArray());
        nbt.put("Islands", islands);
        return nbt;
    }

    @Nullable
    public IslandPos getTeamIsland(String team) {
        return this.skyblockPositions.get(team.toLowerCase());
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

    @Nullable
    public Team createTeamAndJoin(String teamName, PlayerEntity player) {
        return this.createTeamAndJoin(teamName.toLowerCase(), player.getGameProfile().getId());
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, UUID player) {
        if (this.teamExists(teamName)) {
            return null;
        }

        Pair<IslandPos, Team> pair = this.create(player);
        Team team = pair.getRight();
        team.setName(teamName);
        this.markDirty();
        return team;
    }

    public boolean removePlayerFromTeam(PlayerEntity player) {
        return this.removePlayerFromTeam(player.getGameProfile().getId());
    }

    public boolean removePlayerFromTeam(UUID player) {
        Iterator<Map.Entry<String, Team>> itr = this.skyblocks.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Team> entry = itr.next();
            Team team = entry.getValue();
            if (team.hasPlayer(player) && team.removePlayer(player) && team.isEmpty()) {
                this.skyblockPositions.inverse().remove(team.getIsland());
                itr.remove();
                return true;
            }
        }
        return false;
    }

    public boolean deleteTeam(String name) {
        Iterator<Map.Entry<String, Team>> itr = this.skyblocks.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Team> entry = itr.next();
            Team team = entry.getValue();
            if (team.getName().equalsIgnoreCase(name)) {
                this.skyblockPositions.inverse().remove(team.getIsland());
                itr.remove();
                return true;
            }
        }
        return false;
    }

    public boolean deleteTeam(Team team) {
        return this.skyblocks.remove(team.getName()) != null;
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
        return this.skyblocks.containsKey(name);
    }

    public Collection<Team> getTeams() {
        return this.skyblocks.values();
    }

    public Set<BlockPos> getPossibleSpawns(IslandPos pos) {
        if (!this.skyblockPositions.containsValue(pos)) {
            return getPossibleSpawns(pos.getCenter());
        }

        return this.skyblocks.get(this.skyblockPositions.inverse().get(pos)).getPossibleSpawns();
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

    public ServerWorld getWorld() {
        return this.world;
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
