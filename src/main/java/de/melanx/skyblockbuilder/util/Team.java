package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class Team {
    private final SkyblockSavedData data;
    private final Set<UUID> players;
    private final Set<BlockPos> possibleSpawns;
    private final Random random = new Random();
    private IslandPos island;
    private String name;

    public Team(SkyblockSavedData data, IslandPos island) {
        this.data = data;
        this.island = island;
        this.players = new HashSet<>();
        this.possibleSpawns = new HashSet<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
        this.data.markDirty();
    }

    public IslandPos getIsland() {
        return this.island;
    }

    public void setIsland(IslandPos island) {
        this.island = island;
        this.data.markDirty();
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public void setPlayers(Collection<UUID> players) {
        this.players.clear();
        this.players.addAll(players);
        this.data.markDirty();
    }

    public Set<BlockPos> getPossibleSpawns() {
        return this.possibleSpawns;
    }

    public void setPossibleSpawns(Collection<BlockPos> spawns) {
        this.possibleSpawns.clear();
        this.possibleSpawns.addAll(spawns);
        this.data.markDirty();
    }

    public boolean addPlayer(UUID player) {
        boolean added = this.players.add(player);
        this.data.markDirty();
        return added;
    }

    public boolean addPlayer(PlayerEntity player) {
        return this.addPlayer(player.getGameProfile().getId());
    }

    public boolean addPlayers(Collection<UUID> players) {
        boolean added = this.players.addAll(players);
        this.data.markDirty();
        return added;
    }

    public boolean removePlayer(PlayerEntity player) {
        return this.removePlayer(player.getGameProfile().getId());
    }

    public boolean removePlayer(UUID player) {
        boolean removed = this.players.remove(player);
        this.data.markDirty();
        return removed;
    }

    public boolean hasPlayer(UUID player) {
        return this.players.contains(player);
    }

    public boolean hasPlayer(PlayerEntity player) {
        return this.hasPlayer(player.getGameProfile().getId());
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    @Nonnull
    public ServerWorld getWorld() {
        return this.data.getWorld();
    }

    @Nonnull
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.put("Island", this.island.toTag());
        nbt.putString("Name", this.name != null ? this.name : "");

        ListNBT players = new ListNBT();
        for (UUID player : this.players) {
            CompoundNBT playerTag = new CompoundNBT();
            playerTag.putUniqueId("Player", player);

            players.add(playerTag);
        }

        ListNBT spawns = new ListNBT();
        for (BlockPos pos : this.possibleSpawns) {
            CompoundNBT posTag = new CompoundNBT();
            posTag.putDouble("posX", pos.getX() + 0.5);
            posTag.putDouble("posY", pos.getY());
            posTag.putDouble("posZ", pos.getZ() + 0.5);

            spawns.add(posTag);
        }

        nbt.put("Players", players);
        nbt.put("Spawns", spawns);
        return nbt;
    }

    public void deserializeNBT(CompoundNBT nbt) {
        this.island = IslandPos.fromTag(nbt.getCompound("Island"));
        this.name = nbt.getString("Name");

        ListNBT players = nbt.getList("Players", Constants.NBT.TAG_COMPOUND);
        this.players.clear();
        for (INBT player : players) {
            this.players.add(((CompoundNBT) player).getUniqueId("Player"));
        }

        ListNBT spawns = nbt.getList("Spawns", Constants.NBT.TAG_COMPOUND);
        this.possibleSpawns.clear();
        for (INBT pos : spawns) {
            CompoundNBT posTag = (CompoundNBT) pos;
            this.possibleSpawns.add(new BlockPos(posTag.getDouble("posX"), posTag.getDouble("posY"), posTag.getDouble("posZ")));
        }
    }
}
