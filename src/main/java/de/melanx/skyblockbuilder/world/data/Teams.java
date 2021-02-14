package de.melanx.skyblockbuilder.world.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

public class Teams extends WorldSavedData {
    private static final String NAME = "custom_skyblock_teams";

    public Teams() {
        super(NAME);
    }

    public Map<String, Pair<UUID, Set<UUID>>> teams = new HashMap<>();

    public static Teams get(ServerWorld world) {
        return world.getSavedData().getOrCreate(Teams::new, NAME);
    }

    public void add(String name, UUID player) {
        name = name.toLowerCase();
        if (!this.exists(name)) {
            Set<UUID> uuids = new HashSet<>();
            uuids.add(player);
            this.teams.put(name, Pair.of(player, uuids));
        } else {
            Pair<UUID, Set<UUID>> uuids = this.teams.get(name);
            uuids.getValue().add(player);
        }
        this.markDirty();
    }

    private void add(String name, UUID player, boolean leader) {
        name = name.toLowerCase();
        if (leader) {
            if (!this.teams.containsKey(name)) {
                Set<UUID> uuids = new HashSet<>();
                uuids.add(player);
                this.teams.put(name, Pair.of(player, uuids));
            } else {
                Pair<UUID, Set<UUID>> uuids = this.teams.get(name);
                Set<UUID> players = uuids.getValue();
                uuids = Pair.of(player, players);
                this.teams.put(name, uuids);
            }
            this.markDirty();
        } else {
            this.add(name, player);
        }
    }

    public Set<String> getTeams() {
        return this.teams.keySet();
    }

    public void addAll(String name, Collection<UUID> players) {
        name = name.toLowerCase();
        if (!this.teams.containsKey(name)) {
            if (players.isEmpty()) {
                throw new IllegalArgumentException("No players to add.");
            }
            this.teams.put(name, Pair.of(this.getLeader(players), new HashSet<>(players)));
        } else {
            Pair<UUID, Set<UUID>> uuids = this.teams.get(name);
            uuids.getValue().addAll(players);
        }
        this.markDirty();
    }

    public boolean remove(UUID player) {
        for (Map.Entry<String, Pair<UUID, Set<UUID>>> entry : this.teams.entrySet()) {
            if (entry.getValue().getValue().contains(player)) {
                entry.getValue().getValue().remove(player);
                if (this.teams.get(entry.getKey()).getValue().isEmpty()) {
                    this.deleteTeam(entry.getKey());
                    this.markDirty();
                    return true;
                }

                if (entry.getValue().getKey() == player) {
                    Set<UUID> values = entry.getValue().getValue();
                    entry.setValue(Pair.of(this.getLeader(values), values));
                }
                this.markDirty();
                return true;
            }
        }
        return false;
    }

    public void deleteTeam(String name) {
        this.teams.remove(name.toLowerCase());
        this.markDirty();
    }

    public boolean exists(String name) {
        return this.teams.containsKey(name.toLowerCase());
    }

    public boolean hasTeam(PlayerEntity player) {
        for (Map.Entry<String, Pair<UUID, Set<UUID>>> entry : this.teams.entrySet()) {
            if (entry.getValue().getValue().contains(player.getUniqueID())) {
                return true;
            }
        }
        return false;
    }

    private UUID getLeader(Collection<UUID> players) {
        return players.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("No possible leader available."));
    }

    @Override
    public void read(@Nonnull CompoundNBT nbt) {
        if (nbt.contains("players", Constants.NBT.TAG_LIST)) {
            ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < players.size(); i++) {
                CompoundNBT playerTag = players.getCompound(i);
                this.add(playerTag.getString("team"), playerTag.getUniqueId("uuid"), playerTag.getBoolean("leader"));
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT players = new ListNBT();
        for (Map.Entry<String, Pair<UUID, Set<UUID>>> entry : this.teams.entrySet()) {
            for (UUID id : entry.getValue().getValue()) {
                CompoundNBT playerTag = new CompoundNBT();
                playerTag.putUniqueId("uuid", id);
                playerTag.putString("team", entry.getKey());
                playerTag.putBoolean("leader", entry.getValue().getKey() == id);
                players.add(playerTag);
            }
        }
        nbt.put("players", players);
        return nbt;
    }
}
