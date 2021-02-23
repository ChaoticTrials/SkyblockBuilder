package de.melanx.skyblockbuilder.world.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
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

    public static final IslandPos SPAWN_ISLAND = new IslandPos(0, 0);

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
        if (this.skyblockPositions.get("spawn") != null) {
            return this.skyblockPositions.get("spawn");
        }

        Team team = this.createTeam("Spawn");
        assert team != null;
        team.addPlayer(Util.DUMMY_UUID);

        this.markDirty();
        return team.getIsland();
    }

    public Pair<IslandPos, Team> create(String teamName) {
        IslandPos islandPos;
        if (teamName.equalsIgnoreCase("spawn")) {
            islandPos = SPAWN_ISLAND;
        } else {
            do {
                int[] pos = this.spiral.next();
                islandPos = new IslandPos(pos[0], pos[1]);
            } while (this.skyblockPositions.containsValue(islandPos));
        }

        Set<BlockPos> positions = initialPossibleSpawns(islandPos.getCenter());

        Team team = new Team(this, islandPos);
        team.setPossibleSpawns(positions);
        team.setName(teamName);

        this.skyblocks.put(team.getName().toLowerCase(), team);
        this.skyblockPositions.put(team.getName().toLowerCase(), islandPos);

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

            skyblocks.put(team.getName().toLowerCase(), team);
            skyblockPositions.put(team.getName().toLowerCase(), island);
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
        Team team = this.getTeamFromPlayer(player);
        return team != null && team != this.getTeam("spawn");
    }

    public boolean addPlayerToTeam(String teamName, PlayerEntity player) {
        return this.addPlayerToTeam(teamName, player.getGameProfile().getId());
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
    public Team createTeam(String teamName) {
        if (this.teamExists(teamName)) {
            return null;
        }

        Pair<IslandPos, Team> pair = this.create(teamName);
        Team team = pair.getRight();
        List<BlockPos> possibleSpawns = new ArrayList<>(this.getPossibleSpawns(team.getIsland()));
        team.setPossibleSpawns(possibleSpawns);

        PlacementSettings settings = new PlacementSettings();
        TemplateLoader.TEMPLATE.func_237152_b_(this.world, team.getIsland().getCenter(), settings, new Random());

        this.skyblocks.put(team.getName().toLowerCase(), team);
        this.skyblockPositions.put(team.getName().toLowerCase(), team.getIsland());

        this.markDirty();
        return team;
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, PlayerEntity player) {
        return this.createTeamAndJoin(teamName, player.getGameProfile().getId());
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, UUID player) {
        Team team = this.createTeam(teamName);
        if (team == null) return null;

        team.addPlayer(player);
        this.markDirty();
        return team;
    }

    public boolean removePlayerFromTeam(PlayerEntity player) {
        return this.removePlayerFromTeam(player.getGameProfile().getId());
    }

    public boolean removePlayerFromTeam(UUID player) {
        for (Map.Entry<String, Team> entry : this.skyblocks.entrySet()) {
            Team team = entry.getValue();
            if (team.hasPlayer(player)) {
                boolean removed = team.removePlayer(player);
                if (removed) {
                    //noinspection ConstantConditions
                    this.getTeam("spawn").addPlayer(player);
                }
                return removed;
            }
        }
        return false;
    }

    @Nullable
    public Team getTeam(String name) {
        return this.skyblocks.get(name.toLowerCase());
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
        Team removedTeam = this.skyblocks.remove(team.getName());

        //noinspection ConstantConditions
        return removedTeam != null && this.getTeam("spawn").addPlayers(removedTeam.getPlayers());
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
        return this.skyblocks.containsKey(name.toLowerCase());
    }

    public Collection<Team> getTeams() {
        return this.skyblocks.values();
    }

    public Set<BlockPos> getPossibleSpawns(IslandPos pos) {
        if (!this.skyblockPositions.containsValue(pos)) {
            return initialPossibleSpawns(pos.getCenter());
        }

        return this.skyblocks.get(this.skyblockPositions.inverse().get(pos)).getPossibleSpawns();
    }

    public static Set<BlockPos> initialPossibleSpawns(BlockPos center) {
        Set<BlockPos> positions = new HashSet<>();
        for (BlockPos pos : TemplateLoader.SPAWNS) {
            positions.add(center.add(pos.toImmutable()));
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
