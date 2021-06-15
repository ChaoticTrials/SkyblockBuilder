package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import java.util.*;

public class Team {

    private final SkyblockSavedData data;
    private final Set<UUID> players;
    private final Set<BlockPos> possibleSpawns;
    private final Random random = new Random();
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
        PlayerList playerList = this.getWorld().getServer().getPlayerList();
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : players) {
                MineMentionCompat.updateMentions(playerList.getPlayerByUUID(id));
            }
        }
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

    public void addPossibleSpawn(BlockPos pos) {
        this.possibleSpawns.add(pos);
        this.data.markDirty();
    }

    public boolean removePossibleSpawn(BlockPos pos) {
        if (this.possibleSpawns.size() <= 1) {
            return false;
        }

        boolean remove = this.possibleSpawns.remove(pos);
        this.data.markDirty();
        return remove;
    }

    public boolean allowsVisits() {
        return this.allowVisits;
    }

    public boolean toggleAllowVisits() {
        this.allowVisits = !this.allowVisits;
        this.data.markDirty();
        return this.allowVisits;
    }

    public void setAllowVisit(boolean enabled) {
        this.allowVisits = enabled;
        this.data.markDirty();
    }

    public boolean addPlayer(UUID player) {
        boolean added = this.players.add(player);
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.updateMentions(this.getWorld().getServer().getPlayerList().getPlayerByUUID(player));
        }
        this.data.markDirty();
        return added;
    }

    public boolean addPlayer(PlayerEntity player) {
        return this.addPlayer(player.getGameProfile().getId());
    }

    public boolean addPlayers(Collection<UUID> players) {
        boolean added = this.players.addAll(players);
        PlayerList playerList = this.getWorld().getServer().getPlayerList();
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : players) {
                MineMentionCompat.updateMentions(this.getWorld().getServer().getPlayerList().getPlayerByUUID(id));
            }
        }
        this.data.markDirty();
        return added;
    }

    public boolean removePlayer(PlayerEntity player) {
        return this.removePlayer(player.getGameProfile().getId());
    }

    public boolean removePlayer(UUID player) {
        boolean removed = this.players.remove(player);
        if (ModList.get().isLoaded("minemention")) {
            MineMentionCompat.updateMentions(this.getWorld().getServer().getPlayerList().getPlayerByUUID(player));
        }
        this.data.markDirty();
        return removed;
    }

    public void removePlayers(Collection<UUID> players) {
        PlayerList playerList = this.getWorld().getServer().getPlayerList();
        for (UUID id : players) {
            this.players.remove(id);
            if (ModList.get().isLoaded("minemention")) {
                MineMentionCompat.updateMentions(this.getWorld().getServer().getPlayerList().getPlayerByUUID(id));
            }
        }
        this.data.markDirty();
    }

    public void removeAllPlayers() {
        HashSet<UUID> uuids = new HashSet<>(this.players);
        this.players.clear();
        PlayerList playerList = this.getWorld().getServer().getPlayerList();
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : uuids) {
                MineMentionCompat.updateMentions(playerList.getPlayerByUUID(id));
            }
        }
        this.data.markDirty();
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

    public boolean toggleAllowJoinRequest() {
        this.allowJoinRequests = !this.allowJoinRequests;
        this.data.markDirty();
        return this.allowJoinRequests;
    }

    public void setAllowJoinRequest(boolean enabled) {
        this.allowJoinRequests = enabled;
        this.data.markDirty();
    }

    public Set<UUID> getJoinRequests() {
        return this.joinRequests;
    }

    public void addJoinRequest(PlayerEntity player) {
        this.addJoinRequest(player.getGameProfile().getId());
    }

    public void addJoinRequest(UUID id) {
        this.joinRequests.add(id);
        this.data.markDirty();
    }

    public void removeJoinRequest(PlayerEntity player) {
        this.removeJoinRequest(player.getGameProfile().getId());
    }

    public void removeJoinRequest(UUID id) {
        this.joinRequests.remove(id);
        this.data.markDirty();
    }

    public void resetJoinRequests() {
        this.joinRequests.clear();
        this.data.markDirty();
    }

    public void sendJoinRequest(PlayerEntity requestingPlayer) {
        this.addJoinRequest(requestingPlayer.getGameProfile().getId());
        TranslationTextComponent component = new TranslationTextComponent("skyblockbuilder.event.join_request0", requestingPlayer.getDisplayName());
        component.appendSibling(new StringTextComponent("/skyblock team accept " + requestingPlayer.getDisplayName().getString()).setStyle(Style.EMPTY
                .setHoverEvent(InviteCommand.COPY_TEXT)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skyblock team accept " + requestingPlayer.getDisplayName().getString()))
                .mergeWithFormatting(TextFormatting.UNDERLINE, TextFormatting.GOLD)));
        component.appendSibling(new TranslationTextComponent("skyblockbuilder.event.join_request1"));
        this.broadcast(component, Style.EMPTY.applyFormatting(TextFormatting.GOLD));
    }

    @Nonnull
    public ServerWorld getWorld() {
        return this.data.getWorld();
    }

    public void broadcast(IFormattableTextComponent msg, Style style) {
        PlayerList playerList = this.getWorld().getServer().getPlayerList();
        this.players.forEach(uuid -> {
            ServerPlayerEntity player = playerList.getPlayerByUUID(uuid);
            if (player != null) {
                IFormattableTextComponent component = new StringTextComponent("[" + this.name + "] ").setStyle(Style.EMPTY);
                player.sendMessage(component.appendSibling(msg.mergeStyle(style)), uuid);
            }
        });
    }

    @Nonnull
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.put("Island", this.island.toTag());
        nbt.putString("Name", this.name != null ? this.name : "");
        nbt.putBoolean("Visits", this.allowVisits);
        nbt.putBoolean("AllowJoinRequests", this.allowJoinRequests);

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

        ListNBT joinRequests = new ListNBT();
        for (UUID id : this.joinRequests) {
            CompoundNBT idTag = new CompoundNBT();
            idTag.putUniqueId("Id", id);

            joinRequests.add(idTag);
        }

        nbt.put("Players", players);
        nbt.put("Spawns", spawns);
        nbt.put("JoinRequests", joinRequests);
        return nbt;
    }

    public void deserializeNBT(CompoundNBT nbt) {
        this.island = IslandPos.fromTag(nbt.getCompound("Island"));
        this.name = nbt.getString("Name");
        this.allowVisits = nbt.getBoolean("Visits");
        this.allowJoinRequests = nbt.getBoolean("AllowJoinRequests");

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

        if (nbt.contains("JoinRequests")) { // TODO 1.17 remove this check
            ListNBT joinRequests = nbt.getList("JoinRequests", Constants.NBT.TAG_COMPOUND);
            this.joinRequests.clear();
            for (INBT id : joinRequests) {
                CompoundNBT idTag = (CompoundNBT) id;
                this.joinRequests.add(idTag.getUniqueId("Id"));
            }
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
