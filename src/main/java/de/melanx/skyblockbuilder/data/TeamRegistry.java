package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.world.IslandPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamRegistry {

    private final ConcurrentHashMap<UUID, Team> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> byName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<IslandPos, UUID> byPosition = new ConcurrentHashMap<>();

    public void add(Team team) {
        this.byId.put(team.id(), team);
        this.byName.put(team.getName().toLowerCase(Locale.ROOT), team.id());

        if (team.getIsland() != null) {
            this.byPosition.put(team.getIsland(), team.id());
        }
    }

    public void remove(UUID teamId) {
        Team team = this.byId.remove(teamId);

        if (team != null) {
            this.byName.remove(team.getName().toLowerCase(Locale.ROOT));

            if (team.getIsland() != null) {
                this.byPosition.remove(team.getIsland());
            }
        }
    }

    public void rename(UUID teamId, String newName) {
        Team team = this.byId.get(teamId);
        if (team == null) {
            return;
        }

        this.byName.remove(team.getName().toLowerCase(Locale.ROOT));
        team.setName(newName);
        this.byName.put(newName.toLowerCase(Locale.ROOT), teamId);
    }

    @Nullable
    public Team getById(UUID id) {
        return this.byId.get(id);
    }

    @Nullable
    public Team getByName(String name) {
        UUID id = this.byName.get(name.toLowerCase(Locale.ROOT));

        return id != null ? this.byId.get(id) : null;
    }

    @Nullable
    public Team getByPosition(IslandPos pos) {
        UUID id = this.byPosition.get(pos);

        return id != null ? this.byId.get(id) : null;
    }

    public boolean containsId(UUID id) {
        return this.byId.containsKey(id);
    }

    public boolean containsName(String name) {
        return this.byName.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public boolean containsPosition(IslandPos pos) {
        return this.byPosition.containsKey(pos);
    }

    public Collection<Team> all() {
        return Collections.unmodifiableCollection(this.byId.values());
    }

    public int size() {
        return this.byId.size();
    }
}
