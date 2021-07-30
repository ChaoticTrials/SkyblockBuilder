package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.ScreenStructureSaver;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkDirection;

public class SkyNetwork extends NetworkX {

    public SkyNetwork(ModX mod) {
        super(SkyblockBuilder.getInstance());
    }

    @Override
    protected String getProtocolVersion() {
        return "1";
    }

    @Override
    protected void registerPackets() {
        this.register(new ClickScreenButtonHandler.ClickScreenButtonSerializer(), () -> ClickScreenButtonHandler::handle, NetworkDirection.PLAY_TO_SERVER);
    }

    public void handleButtonClick(ItemStack stack, ScreenStructureSaver.Button button, String name) {
        this.instance.sendToServer(new ClickScreenButtonHandler.Message(stack, button, name));
    }
}
