package de.melanx.skyblockbuilder.events;

import net.minecraftforge.eventbus.api.Event;

/**
 * SkyblockCreateTeamEvent is fired whenever a command creates a new team.<br>
 * <br>
 * This event is {@link net.minecraftforge.eventbus.api.Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link net.minecraftforge.eventbus.api.Event.HasResult}<br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}
 */
public class SkyblockCreateTeamEvent extends Event {

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

    @Override
    public boolean isCancelable() {
        return true;
    }
}
