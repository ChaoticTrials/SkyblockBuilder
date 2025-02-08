package de.melanx.skyblockbuilder.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SkyMeta {

    private final Set<UUID> previousTeamIds = Sets.newHashSet();
    private final List<UUID> invites = Lists.newArrayList();
    private final SkyblockSavedData data;
    private UUID owner;
    private UUID teamId = SkyblockSavedData.SPAWN_ID;
    private long lastHomeTeleport;
    private long lastSpawnTeleport;
    private long lastVisitTeleport;

    public static SkyMeta get(SkyblockSavedData data, @Nonnull CompoundTag nbt) {
        return new SkyMeta(data, null).load(nbt);
    }

    public SkyMeta(SkyblockSavedData data, UUID owner) {
        this.data = data;
        this.owner = owner;
    }

    @Nonnull
    public UUID getOwner() {
        return this.owner;
    }

    @Nonnull
    public UUID getTeamId() {
        return this.teamId;
    }

    public void setTeamId(@Nonnull UUID id) {
        this.teamId = id;
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public Set<UUID> getPreviousTeamIds() {
        return this.previousTeamIds;
    }

    public void addPreviousTeamId(@Nonnull UUID id) {
        this.previousTeamIds.add(id);
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public List<UUID> getInvites() {
        return this.invites;
    }

    public void addInvite(@Nonnull UUID teamId) {
        this.invites.add(teamId);
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public void removeInvite(@Nonnull UUID teamId) {
        this.invites.remove(teamId);
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public void resetInvites() {
        this.invites.clear();
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public long getLastTeleport(TeleportType type) {
        return switch (type) {
            case SPAWN -> this.lastSpawnTeleport;
            case HOME -> this.lastHomeTeleport;
            case VISIT -> this.lastVisitTeleport;
        };
    }

    public void setLastTeleport(TeleportType type, long gameTime) {
        switch (type) {
            case SPAWN -> this.lastSpawnTeleport = gameTime;
            case HOME -> this.lastHomeTeleport = gameTime;
            case VISIT -> this.lastVisitTeleport = gameTime;
        }

        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public boolean canTeleport(TeleportType type, long gameTime) {
        long lastTeleport = this.getLastTeleport(type);
        int cooldown = switch (type) {
            case SPAWN -> PermissionsConfig.Teleports.spawnCooldown;
            case HOME -> PermissionsConfig.Teleports.homeCooldown;
            case VISIT -> PermissionsConfig.Teleports.visitCooldown;
        };

        return (lastTeleport == 0 ? cooldown : gameTime) - lastTeleport >= cooldown;
    }

    public SkyMeta load(@Nonnull CompoundTag nbt) {
        this.owner = nbt.getUUID("OwnerId");
        this.teamId = nbt.getUUID("TeamId");

        this.previousTeamIds.clear();
        for (Tag tag : nbt.getList("PreviousTeamIds", Tag.TAG_INT_ARRAY)) {
            this.previousTeamIds.add(NbtUtils.loadUUID(tag));
        }

        this.invites.clear();
        for (Tag tag : nbt.getList("Invitations", Tag.TAG_INT_ARRAY)) {
            this.invites.add(NbtUtils.loadUUID(tag));
        }

        this.lastHomeTeleport = nbt.getLong("LastHomeTeleport");
        this.lastSpawnTeleport = nbt.getLong("LastSpawnTeleport");
        this.lastVisitTeleport = nbt.getLong("LastVisitTeleport");

        return this;
    }

    @Nonnull
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("OwnerId", this.owner);
        nbt.putUUID("TeamId", this.teamId);

        ListTag prevTeamIds = new ListTag();
        for (UUID id : this.previousTeamIds) {
            prevTeamIds.add(NbtUtils.createUUID(id));
        }

        ListTag invitationTeams = new ListTag();
        for (UUID id : this.invites) {
            invitationTeams.add(NbtUtils.createUUID(id));
        }

        nbt.put("PreviousTeamIds", prevTeamIds);
        nbt.put("Invitations", invitationTeams);
        nbt.putLong("LastHomeTeleport", this.lastHomeTeleport);
        nbt.putLong("LastSpawnTeleport", this.lastSpawnTeleport);
        nbt.putLong("LastVisitTeleport", this.lastVisitTeleport);
        return nbt;
    }

    public enum TeleportType {
        SPAWN,
        HOME,
        VISIT
    }
}
