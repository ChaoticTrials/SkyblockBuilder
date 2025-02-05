package de.melanx.skyblockbuilder.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * SkyblockCreateTeamEvent is fired whenever a command creates a new team.<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class SkyblockCreateTeamEvent extends Event implements ICancellableEvent {

    private final String name;

    /**
     * @param name The name for the new team
     */
    public SkyblockCreateTeamEvent(String name) {
        this.name = name;
    }

    /**
     * @return Team name
     */
    public String getName() {
        return this.name;
    }
}
