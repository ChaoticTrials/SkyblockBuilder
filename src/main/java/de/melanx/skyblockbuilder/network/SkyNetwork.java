package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.ScreenStructureSaver;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class SkyNetwork extends NetworkX {

    public SkyNetwork() {
        super(SkyblockBuilder.getInstance());
    }

    @Override
    protected String getProtocolVersion() {
        return "1";
    }

    @Override
    protected void registerPackets() {
        this.register(new ClickScreenButtonHandler.ClickScreenButtonSerializer(), () -> ClickScreenButtonHandler::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new SkyblockDataUpdateHandler.Serializer(), () -> SkyblockDataUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void updateData(Level level) {
        if (!level.isClientSide) {
            this.instance.send(PacketDistributor.ALL.noArg(), new SkyblockDataUpdateHandler.Message(SkyblockSavedData.get(level)));
        }
    }

    public void updateData(Player player) {
        if (!player.getCommandSenderWorld().isClientSide) {
            this.instance.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SkyblockDataUpdateHandler.Message(SkyblockSavedData.get(player.getCommandSenderWorld())));
        }
    }

    public void handleButtonClick(ItemStack stack, ScreenStructureSaver.Button button, String name) {
        this.instance.sendToServer(new ClickScreenButtonHandler.Message(stack, button, name));
    }
}
