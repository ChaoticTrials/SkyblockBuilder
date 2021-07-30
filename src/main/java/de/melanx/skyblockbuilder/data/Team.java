package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.world.IslandPos;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {

    private final SkyblockSavedData data;
    private final Set<UUID> players;
    private final Set<BlockPos> possibleSpawns;
    private final Set<UUID> joinRequests = new HashSet<>();
    private IslandPos island;
    private String name;
    private boolean allowVisits;
    private boolean allowJoinRequests;

    public Team(SkyblockSavedData data, IslandPos island) {
        this.data = data;
        this.island = island;
        this.players = new HashSet<>();
        this.possibleSpawns = new HashSet<>();
        this.allowVisits = false;
    }

    public boolean isSpawn() {
        return this.name.equalsIgnoreCase("spawn");
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.data.setDirty();
    }

    public IslandPos getIsland() {
        return this.island;
    }

    public void setIsland(IslandPos island) {
        this.island = island;
        this.data.setDirty();
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public void setPlayers(Collection<UUID> players) {
        this.players.clear();
        PlayerList playerList = this.getLevel().getServer().getPlayerList();
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : players) {
                MineMentionCompat.updateMentions(playerList.getPlayer(id));
            }
        }
        this.players.addAll(players);
        this.data.setDirty();
    }

    public Set<BlockPos> getPossibleSpawns() {
        return this.possibleSpawns;
    }

    public void setPossibleSpawns(Collection<BlockPos> spawns) {
        this.possibleSpawns.clear();
        this.possibleSpawns.addAll(spawns);
        this.data.setDirty();
    }

    public void addPossibleSpawn(BlockPos pos) {
        this.possibleSpawns.add(pos);
        this.data.setDirty();
    }

    public boolean removePossibleSpawn(BlockPos pos) {
        if (this.possibleSpawns.size() <= 1) {
            return false;
        }

        boolean remove = this.possibleSpawns.remove(pos);
        this.data.setDirty();
        return remove;
    }

    public boolean allowsVisits() {
        return this.allowVisits;
    }

    public boolean toggleAllowVisits() {
        this.allowVisits = !this.allowVisits;
        this.data.setDirty();
        return this.allowVisits;
    }

    public void setAllowVisit(boolean enabled) {
        this.allowVisits = enabled;
        this.data.setDirty();
    }

    public boolean addPlayer(UUID player) {
        boolean added = this.players.add(player);
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(player));
        }
        this.data.setDirty();
        return added;
    }

    public boolean addPlayer(Player player) {
        return this.addPlayer(player.getGameProfile().getId());
    }

    public boolean addPlayers(Collection<UUID> players) {
        boolean added = this.players.addAll(players);
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : players) {
                MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(id));
            }
        }
        this.data.setDirty();
        return added;
    }

    public boolean removePlayer(Player player) {
        return this.removePlayer(player.getGameProfile().getId());
    }

    public boolean removePlayer(UUID player) {
        boolean removed = this.players.remove(player);
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(player));
        }
        this.data.setDirty();
        return removed;
    }

    public void removePlayers(Collection<UUID> players) {
        for (UUID id : players) {
            this.players.remove(id);
            if (ModList.get().isLoaded("minemention")) {
                MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(id));
            }
        }
        this.data.setDirty();
    }

    public void removeAllPlayers() {
        HashSet<UUID> uuids = new HashSet<>(this.players);
        this.players.clear();
        PlayerList playerList = this.getLevel().getServer().getPlayerList();
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : uuids) {
                MineMentionCompat.updateMentions(playerList.getPlayer(id));
            }
        }
        this.data.setDirty();
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

    public boolean toggleAllowJoinRequest() {
        this.allowJoinRequests = !this.allowJoinRequests;
        this.data.setDirty();
        return this.allowJoinRequests;
    }

    public void setAllowJoinRequest(boolean enabled) {
        this.allowJoinRequests = enabled;
        this.data.setDirty();
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

    @Nonnull
    public ServerLevel getLevel() {
        return this.data.getLevel();
    }

    public void broadcast(MutableComponent msg, Style style) {
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

        nbt.put("Island", this.island.toTag());
        nbt.putString("Name", this.name != null ? this.name : "");
        nbt.putBoolean("Visits", this.allowVisits);
        nbt.putBoolean("AllowJoinRequests", this.allowJoinRequests);

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
        this.island = IslandPos.fromTag(nbt.getCompound("Island"));
        this.name = nbt.getString("Name");
        this.allowVisits = nbt.getBoolean("Visits");
        this.allowJoinRequests = nbt.getBoolean("AllowJoinRequests");

        ListTag players = nbt.getList("Players", Constants.NBT.TAG_COMPOUND);
        this.players.clear();
        for (Tag player : players) {
            this.players.add(((CompoundTag) player).getUUID("Player"));
        }

        ListTag spawns = nbt.getList("Spawns", Constants.NBT.TAG_COMPOUND);
        this.possibleSpawns.clear();
        for (Tag pos : spawns) {
            CompoundTag posTag = (CompoundTag) pos;
            this.possibleSpawns.add(new BlockPos(posTag.getDouble("posX"), posTag.getDouble("posY"), posTag.getDouble("posZ")));
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
