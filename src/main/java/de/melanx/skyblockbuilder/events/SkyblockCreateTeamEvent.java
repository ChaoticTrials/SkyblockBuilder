package de.melanx.skyblockbuilder.events;

import net.minecraftforge.eventbus.api.Event;

public class SkyblockCreateTeamEvent extends Event {
    
    private final String name;

    public SkyblockCreateTeamEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
