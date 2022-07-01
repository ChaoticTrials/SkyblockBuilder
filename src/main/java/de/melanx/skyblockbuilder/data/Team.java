package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Team {

    private final SkyblockSavedData data;
    private final Set<UUID> players;
    private final Set<UUID> joinRequests;
    private final Set<BlockPos> possibleSpawns;
    private UUID teamId;
    private IslandPos island;
    private String name;
    private boolean allowVisits;
    private boolean allowJoinRequests;
    private long createdAt;
    private long lastChanged;
    private WorldUtil.Directions direction;

    private Team(SkyblockSavedData data) {
        this(data, null, null, null);
    }

    @RemoveIn(minecraft = "1.19")
    @Deprecated(forRemoval = true)
    public Team(SkyblockSavedData data, IslandPos island) {
        this(data, island, UUID.randomUUID(), WorldUtil.Directions.SOUTH);
    }

    @RemoveIn(minecraft = "1.19")
    @Deprecated(forRemoval = true)
    public Team(SkyblockSavedData data, IslandPos island, UUID teamId) {
        this(data, island, teamId, WorldUtil.Directions.SOUTH);
    }

    public Team(SkyblockSavedData data, IslandPos island, WorldUtil.Directions direction) {
        this(data, island, UUID.randomUUID(), direction);
    }

    public Team(SkyblockSavedData data, IslandPos island, UUID teamId, WorldUtil.Directions direction) {
        this.data = data;
        this.island = island;
        this.players = new HashSet<>();
        this.possibleSpawns = new HashSet<>();
        this.joinRequests = new HashSet<>();
        this.teamId = teamId;
        this.allowVisits = false;
        this.createdAt = System.currentTimeMillis();
        this.lastChanged = System.currentTimeMillis();
        this.direction = direction;
    }

    public static Team create(SkyblockSavedData data, CompoundTag tag) {
        Team team = new Team(data);
        team.deserializeNBT(tag);

        return team;
    }

    public boolean isSpawn() {
        return Objects.equals(this.teamId, SkyblockSavedData.SPAWN_ID);
    }

    public String getName() {
        return this.name;
    }

    public UUID getId() {
        return this.teamId;
    }

    public void setName(String name) {
        this.name = name;
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    public IslandPos getIsland() {
        return this.island;
    }

    public void setIsland(IslandPos island) {
        this.island = island;
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public void setPlayers(Collection<UUID> players) {
        this.players.clear();
        //noinspection ConstantConditions
        PlayerList playerList = this.getLevel().getServer().getPlayerList();
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : players) {
                MineMentionCompat.updateMentions(playerList.getPlayer(id));
            }
        }
        this.players.addAll(players);
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    public Set<BlockPos> getPossibleSpawns() {
        return this.possibleSpawns;
    }

    public void setPossibleSpawns(Collection<BlockPos> spawns) {
        this.possibleSpawns.clear();
        this.possibleSpawns.addAll(spawns);
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    public void addPossibleSpawn(BlockPos pos) {
        this.possibleSpawns.add(pos);
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    public boolean removePossibleSpawn(BlockPos pos) {
        if (this.possibleSpawns.size() <= 1) {
            return false;
        }

        boolean remove = this.possibleSpawns.remove(pos);
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();

        return remove;
    }

    public boolean allowsVisits() {
        return this.allowVisits;
    }

    public boolean toggleAllowVisits() {
        this.allowVisits = !this.allowVisits;
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
        return this.allowVisits;
    }

    public void setAllowVisit(boolean enabled) {
        if (this.allowVisits != enabled) {
            this.allowVisits = enabled;
            this.lastChanged = System.currentTimeMillis();
            this.data.setDirty();
        }
    }

    public boolean addPlayer(UUID player) {
        boolean added = this.players.add(player);
        if (added) {
            if (ModList.get().isLoaded("minemention")) {
                //noinspection ConstantConditions
                MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(player));
            }
            this.lastChanged = System.currentTimeMillis();
            this.data.getOrCreateMetaInfo(player).setTeamId(this.teamId);
            this.data.setDirty();
        }
        return added;
    }

    public boolean addPlayer(Player player) {
        return this.addPlayer(player.getGameProfile().getId());
    }

    public boolean addPlayers(Collection<UUID> players) {
        boolean added = this.players.addAll(players);
        if (added) {
            if (ModList.get().isLoaded("minemention")) {
                for (UUID id : players) {
                    //noinspection ConstantConditions
                    MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(id));
                    this.data.getOrCreateMetaInfo(id).setTeamId(this.teamId);
                }
            }
            this.lastChanged = System.currentTimeMillis();
            this.data.setDirty();
        }

        return added;
    }

    public boolean removePlayer(Player player) {
        return this.removePlayer(player.getGameProfile().getId());
    }

    public boolean removePlayer(UUID player) {
        boolean removed = this.players.remove(player);
        if (ModList.get().isLoaded("minemention")) {
            //noinspection ConstantConditions
            MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(player));
        }
        if (removed) {
            this.data.getOrCreateMetaInfo(player).addPreviousTeamId(this.teamId);
            this.lastChanged = System.currentTimeMillis();
        }
        this.data.setDirty();

        return removed;
    }

    public void removePlayers(Collection<UUID> players) {
        for (UUID id : players) {
            boolean removed = this.players.remove(id);
            if (ModList.get().isLoaded("minemention")) {
                //noinspection ConstantConditions
                MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(id));
            }
            if (removed) {
                this.data.getOrCreateMetaInfo(id).addPreviousTeamId(this.teamId);
            }
        }
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    public void removeAllPlayers() {
        this.removePlayers(this.players);
    }

    public boolean hasPlayer(UUID player) {
        return this.players.contains(player);
    }

    public boolean hasPlayer(Player player) {
        return this.hasPlayer(player.getGameProfile().getId());
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public boolean allowsJoinRequests() {
        return this.allowJoinRequests;
    }

    public boolean toggleAllowJoinRequest() {
        this.allowJoinRequests = !this.allowJoinRequests;
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
        return this.allowJoinRequests;
    }

    public void setAllowJoinRequest(boolean enabled) {
        if (this.allowJoinRequests != enabled) {
            this.allowJoinRequests = enabled;
            this.lastChanged = System.currentTimeMillis();
            this.data.setDirty();
        }
    }

    public Set<UUID> getJoinRequests() {
        return this.joinRequests;
    }

    public void addJoinRequest(Player player) {
        this.addJoinRequest(player.getGameProfile().getId());
    }

    public void addJoinRequest(UUID id) {
        this.joinRequests.add(id);
        this.data.setDirty();
    }

    public void removeJoinRequest(Player player) {
        this.removeJoinRequest(player.getGameProfile().getId());
    }

    public void removeJoinRequest(UUID id) {
        this.joinRequests.remove(id);
        this.data.setDirty();
    }

    public void resetJoinRequests() {
        this.joinRequests.clear();
        this.data.setDirty();
    }

    public void sendJoinRequest(Player requestingPlayer) {
        this.addJoinRequest(requestingPlayer.getGameProfile().getId());
        TranslatableComponent component = new TranslatableComponent("skyblockbuilder.event.join_request0", requestingPlayer.getDisplayName());
        component.append(new TextComponent("/skyblock team accept " + requestingPlayer.getDisplayName().getString()).setStyle(Style.EMPTY
                .withHoverEvent(InviteCommand.COPY_TEXT)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skyblock team accept " + requestingPlayer.getDisplayName().getString()))
                .applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.GOLD)));
        component.append(new TranslatableComponent("skyblockbuilder.event.join_request1"));
        this.broadcast(component, Style.EMPTY.applyFormat(ChatFormatting.GOLD));
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getLastChanged() {
        return this.lastChanged;
    }

    public void updateLastChanged() {
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    public void setDirection(WorldUtil.Directions direction) {
        this.direction = direction;
    }

    public WorldUtil.Directions getDirection() {
        return this.direction;
    }

    @Nullable
    public ServerLevel getLevel() {
        return this.data.getLevel();
    }

    public void broadcast(MutableComponent msg, Style style) {
        if (this.getLevel() == null || this.getLevel().isClientSide) {
            return;
        }

        PlayerList playerList = this.getLevel().getServer().getPlayerList();
        this.players.forEach(uuid -> {
            ServerPlayer player = playerList.getPlayer(uuid);
            if (player != null) {
                MutableComponent component = new TextComponent("[" + this.name + "] ").setStyle(Style.EMPTY);
                player.sendMessage(component.append(msg.withStyle(style)), uuid);
            }
        });
    }

    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putUUID("TeamId", this.teamId);
        nbt.put("Island", this.island.toTag());
        nbt.putString("Name", this.name != null ? this.name : "");
        nbt.putBoolean("Visits", this.allowVisits);
        nbt.putBoolean("AllowJoinRequests", this.allowJoinRequests);
        nbt.putLong("CreatedAt", this.createdAt);
        nbt.putLong("LastChanged", this.lastChanged);
        nbt.putString("SpawnPointDirection", this.direction.name());

        ListTag players = new ListTag();
        for (UUID player : this.players) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", player);

            players.add(playerTag);
        }

        ListTag spawns = new ListTag();
        for (BlockPos pos : this.possibleSpawns) {
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("posX", pos.getX() + 0.5);
            posTag.putDouble("posY", pos.getY());
            posTag.putDouble("posZ", pos.getZ() + 0.5);

            spawns.add(posTag);
        }

        ListTag joinRequests = new ListTag();
        for (UUID id : this.joinRequests) {
            CompoundTag idTag = new CompoundTag();
            idTag.putUUID("Id", id);

            joinRequests.add(idTag);
        }

        nbt.put("Players", players);
        nbt.put("Spawns", spawns);
        nbt.put("JoinRequests", joinRequests);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.teamId = nbt.getUUID("TeamId");
        this.island = IslandPos.fromTag(nbt.getCompound("Island"));
        this.name = nbt.getString("Name");
        this.allowVisits = nbt.getBoolean("Visits");
        this.allowJoinRequests = nbt.getBoolean("AllowJoinRequests");
        this.createdAt = nbt.getLong("CreatedAt");
        this.lastChanged = nbt.getLong("LastChanged");
        this.direction = WorldUtil.Directions.valueOf(nbt.getString("SpawnPointDirection"));

        ListTag players = nbt.getList("Players", Tag.TAG_COMPOUND);
        this.players.clear();
        for (Tag player : players) {
            this.players.add(((CompoundTag) player).getUUID("Player"));
        }

        ListTag spawns = nbt.getList("Spawns", Tag.TAG_COMPOUND);
        this.possibleSpawns.clear();
        for (Tag pos : spawns) {
            CompoundTag posTag = (CompoundTag) pos;
            this.possibleSpawns.add(new BlockPos(posTag.getDouble("posX"), posTag.getDouble("posY"), posTag.getDouble("posZ")));
        }

        ListTag joinRequests = nbt.getList("JoinRequests", Tag.TAG_COMPOUND);
        this.joinRequests.clear();
        for (Tag id : joinRequests) {
            this.joinRequests.add(((CompoundTag) id).getUUID("Id"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Team)) {
            return false;
        }

        Team team = (Team) o;
        return this.name.equals(team.name) && this.island.equals(team.island);
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result * this.island.hashCode();
        return result;
    }
}
